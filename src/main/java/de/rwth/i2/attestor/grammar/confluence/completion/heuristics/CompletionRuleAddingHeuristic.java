package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.GrammarRuleOriginal;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

import java.util.*;

/**
 * Abstract class for all heuristics that try to add one or more rules for every critical pair.
 *
 * TODO: Check that the added rules do not violate the local concretisation property.
 */
public abstract class CompletionRuleAddingHeuristic implements CompletionHeuristic {

    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        return new Iterable<CompletionState>() {
            @Override
            public Iterator<CompletionState> iterator() {
                Iterator<CriticalPair> criticalPairIterator = state.getCriticalPairs().iterator();

                return new Iterator<CompletionState>() {
                    Iterator<Collection<Pair<Nonterminal, HeapConfiguration>>> newRuleIterator = Collections.emptyIterator();
                    CompletionState nextCompletionState = computeNext();

                    private CompletionState computeNext() {
                        while (!newRuleIterator.hasNext()) {
                            if (criticalPairIterator.hasNext()) {
                                newRuleIterator = addNewRules(criticalPairIterator.next()).iterator();
                            } else {
                                return null;
                            }
                        }
                        Collection<Pair<Nonterminal, HeapConfiguration>> newRules = newRuleIterator.next();

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
                        return new CompletionState(newGrammar);
                    }

                    @Override
                    public boolean hasNext() {
                        return nextCompletionState != null;
                    }

                    @Override
                    public CompletionState next() {
                        CompletionState oldCompletionState = nextCompletionState;
                        nextCompletionState = computeNext();
                        return oldCompletionState;
                    }
                };
            }
        };
    }

    /**
     * Tries to compute rules that will remove the given critical pair
     * @param criticalPair  The critical pair which should be removed
     * @return Multiple collections of rules. Each rule collection removes the given critical pair. Returns empty iterator, if critical pair cannot be removed
     */
    abstract Iterable<Collection<Pair<Nonterminal, HeapConfiguration>>> addNewRules(CriticalPair criticalPair);

}
