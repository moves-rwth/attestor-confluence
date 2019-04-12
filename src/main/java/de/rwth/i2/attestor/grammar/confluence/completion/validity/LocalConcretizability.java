package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.typedness.GrammarTypedness;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * TODO: Can removing rules lead to not local concretizability?
 */
public class LocalConcretizability implements GrammarValidity {

    /**
     * Assuming the old grammar was locally concretizable is the new grammar locally concretizable?
     * Note: Might return false negatives (returns false even though locally concretizable)
     *
     * For all tentacles of all LHS of newRules:
     * 1. Calculate the set of outgoing selectors for each RHS at the current tentacle
     * 2. If there is a selector edge that is not in one RHS, check if the RHS can create the selector edge using further rule applications -> violation
     */
    @Override
    public boolean isValid(CompletionState oldCompletionState, CompletionState newCompletionState) {
        Collection<GrammarRuleOriginal> newGrammarRules = GrammarValidity.getNewRules(oldCompletionState, newCompletionState);
        if (newGrammarRules.size() == 0) {
            // No rules were added -> Local concretizability remains intact (TODO is this correct?)
            return true;
        } else {
            // Some rules were added -> Check if they don't violate local concretizability
            Grammar newGrammar = getGrammarFromOriginalRules(newCompletionState.getGrammar().getOriginalGrammarRules());
            GrammarTypedness newTypes = new GrammarTypedness(newGrammar);

            Grammar newRules = getGrammarFromOriginalRules(newGrammarRules);

            for (Nonterminal nt : newRules.getAllLeftHandSides()) {
                for (int tentacle=0; tentacle < nt.getRank(); tentacle++) {
                    Set<SelectorLabel> allSelectors = newTypes.getTentacleType(nt, tentacle).getAllTypes();
                    for (HeapConfiguration newRhs : newRules.getRightHandSidesFor(nt)) {
                        int node = newRhs.externalNodeAt(tentacle);
                        Set<SelectorLabel> directNewSelectors = new HashSet<>(newRhs.selectorLabelsOf(node));
                        if (!allSelectors.equals(directNewSelectors)) {
                            Set<SelectorLabel> missingSelectors = new HashSet<>(allSelectors);
                            missingSelectors.removeAll(directNewSelectors);
                            // All missing selectors should not be creatable at 'node'
                            Set<SelectorLabel> recursiveNewSelectors = newTypes.getTypesAtNode(newRhs, node);
                            if (!Collections.disjoint(missingSelectors, recursiveNewSelectors)) {
                                return false;
                            }

                        }
                    }
                }
            }

            return true;
        }
    }

    /**
     * Note: Does not compute collapsed rules, because they are not needed here
     */
    private static Grammar getGrammarFromOriginalRules(Iterable<GrammarRuleOriginal> rules) {
        GrammarBuilder builder = new GrammarBuilder();
        for (GrammarRuleOriginal rule : rules) {
            builder.addRule(rule.getNonterminal(), rule.getHeapConfiguration());
        }
        return builder.build();
    }

}
