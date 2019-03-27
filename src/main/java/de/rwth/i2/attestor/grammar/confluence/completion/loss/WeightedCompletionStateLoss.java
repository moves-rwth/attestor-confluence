package de.rwth.i2.attestor.grammar.confluence.completion.loss;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

public class WeightedCompletionStateLoss implements CompletionStateLoss {
    int numCriticalPairWeight;  // This should be the main factor, as we want to minimize the number critical pairs
    int numExternalNodesWeight;  // Fewer external nodes decrease the likelyhood of new critical pairs
    int numRulesWeight;  // A smaller grammar means a faster analysis later

    public WeightedCompletionStateLoss(int numCriticalPairWeight, int numExternalNodesWeight, int numRulesWeight) {
        this.numCriticalPairWeight = numCriticalPairWeight;
        this.numExternalNodesWeight = numExternalNodesWeight;
        this.numRulesWeight = numRulesWeight;
    }

    @Override
    public int getLoss(CompletionState state) {
        int loss = state.getCriticalPairs().size() * numCriticalPairWeight + state.getGrammarRules().size() * numRulesWeight;
        if (numExternalNodesWeight != 0) {
            int numberExternalNodes = 0;
            for (GrammarRule rule : state.getGrammarRules()) {
                numberExternalNodes += rule.getHeapConfiguration().countExternalNodes();
            }
            loss += numExternalNodesWeight * numberExternalNodes;
        }
        return loss;
    }
}
