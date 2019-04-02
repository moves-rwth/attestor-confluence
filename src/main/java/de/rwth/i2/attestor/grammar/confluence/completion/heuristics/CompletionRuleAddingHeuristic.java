package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Abstract class for all heuristics that try to add one or more rules for every critical pair.
 */
public abstract class CompletionRuleAddingHeuristic implements CompletionHeuristic {

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        Collection<CompletionState> result = new ArrayList<>();
        for (CriticalPair criticalPair : state.getCriticalPairs()) {
            for (Collection<Pair<Nonterminal, HeapConfiguration>> newRules : addNewRules(criticalPair)) {
                NamedGrammar grammar = state.getGrammar();
                Collection<GrammarRuleOriginal> newGrammarRules = new ArrayList<>();

                int originialRuleIdx = grammar.getMaxOriginalRuleIdx() + 1;
                for (Pair<Nonterminal, HeapConfiguration> newRulePair : newRules) {
                    Nonterminal nt = newRulePair.first();
                    HeapConfiguration rhs = newRulePair.second();
                    newGrammarRules.add(new GrammarRuleOriginal(grammar.getGrammarName(), nt, rhs, originialRuleIdx));
                    originialRuleIdx++;
                }

                // Add state with the new rules
                NamedGrammar newGrammar = grammar.getModifiedGrammar(Collections.EMPTY_LIST, newGrammarRules, grammar.getAbstractionBlockingHeapConfigurations());
                result.add(new CompletionState(newGrammar));
            }
        }
        return result;
    }

    /**
     * Tries to compute rules that will remove the given critical pair
     * @param criticalPair  The critical pair which should be removed
     * @return null, if the heuristic cannot remove the critical pair, otherwise a collection of rule collections.
     */
    abstract Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair);


    static boolean isHandle(HeapConfiguration hc) {
        return hc.countNonterminalEdges() == 1 && hc.countNonterminalEdges() == 0;
    }
}
