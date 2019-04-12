package de.rwth.i2.attestor.grammar.confluence.completion.validity;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;

import java.util.Collection;
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
            Grammar oldRules = getGrammarFromOriginalRules(oldCompletionState.getGrammar().getOriginalGrammarRules());
            Grammar newRules = getGrammarFromOriginalRules(newGrammarRules);
            for (Nonterminal nt : newRules.getAllLeftHandSides()) {
                for (int tentacle=0; tentacle < nt.getRank(); tentacle++) {
                    Set<SelectorLabel> directReachableSelectors = new HashSet<>();
                    Set<SelectorLabel>
                }
            }

            // TODO: Implement

            throw new UnsupportedOperationException("Not implemented yet");
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

    /**
     * Returns a set of all possible outgoing selector edges that can be created at the given tentacle
     */
    private static Set<SelectorLabel> getType(Grammar grammar, Nonterminal nt, int tentacle) {

    }

    private static Set<SelectorLabel> getImmediateSelectors(Grammar grammar, Nonterminal nt, int tentacle) {
        Set<SelectorLabel> result = new HashSet<>();
        for (Grammar grammar : grammar.)
        return result;
    }




}
