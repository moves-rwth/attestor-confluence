package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.SubsetGrammar;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
import de.rwth.i2.attestor.util.Pair;

public class FindViolatingCollapsedRules {
    public FindViolatingCollapsedRules(NamedGrammar grammar) {
        System.out.println("Start of violation check.");
        SubsetGrammar currentGrammarSubset = new SubsetGrammar(grammar, null);
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(currentGrammarSubset);
        if (criticalPairFinder.getJoinabilityResult() != Joinability.STRONGLY_JOINABLE) {
            System.out.println("Uncollapsed grammar is not confluent");
        } else {
            for (int originalRuleIdx = 0; originalRuleIdx < grammar.numberOriginalRules(); originalRuleIdx++) {
                for (int collapsedRuleIdx = 0; collapsedRuleIdx < grammar.numberCollapsedRules(originalRuleIdx); collapsedRuleIdx++) {
                    currentGrammarSubset = new SubsetGrammar(grammar, new Pair<>(originalRuleIdx, collapsedRuleIdx));
                    criticalPairFinder = new CriticalPairFinder(currentGrammarSubset);
                    if (criticalPairFinder.getJoinabilityResult() != Joinability.STRONGLY_JOINABLE) {
                        System.out.println(String.format("Rule %d.%d violates confluence", originalRuleIdx, currentGrammarSubset));
                    }
                }
            }
        }
        System.out.println("End of violation check.");
    }

    public static void main(String[] args) {
        NamedGrammar dllGrammar = ConfluenceTool.parseGrammar("DLList");
        new FindViolatingCollapsedRules(dllGrammar);
    }

}
