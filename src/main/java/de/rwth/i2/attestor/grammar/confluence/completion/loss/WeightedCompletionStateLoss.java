package de.rwth.i2.attestor.grammar.confluence.completion.loss;

import de.rwth.i2.attestor.grammar.GrammarRule;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;

/**
 * A customizable loss function that takes the number of critical pairs, number external nodes and number of rules into
 * account. The weights must be specified, when creating the WeightedCompletionStateLoss object.
 */
public class WeightedCompletionStateLoss implements CompletionStateLoss {
    double numCriticalPairWeight;  // This should be the main factor, as we want to minimize the number critical pairs
    double numExternalNodesWeight;  // Fewer external nodes decrease the likelihood of new critical pairs
    double numRulesWeight;  // A smaller grammar means a faster analysis later

    public WeightedCompletionStateLoss(double numCriticalPairWeight, double numExternalNodesWeight, double numRulesWeight) {
        this.numCriticalPairWeight = numCriticalPairWeight;
        this.numExternalNodesWeight = numExternalNodesWeight;
        this.numRulesWeight = numRulesWeight;
    }

    @Override
    public double getLoss(CompletionState state) {
        double loss = state.getCriticalPairs().size() * numCriticalPairWeight + state.getGrammar().getActiveRules().size() * numRulesWeight;
        if (numExternalNodesWeight != 0) {
            int numberExternalNodes = 0;
            for (GrammarRule rule : state.getGrammar().getActiveRules()) {
                numberExternalNodes += rule.getHeapConfiguration().countExternalNodes();
            }
            loss += numExternalNodesWeight * numberExternalNodes;
        }
        return loss;
    }
}
