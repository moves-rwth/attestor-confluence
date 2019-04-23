package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import gnu.trove.list.array.TIntArrayList;

import java.util.Collections;
import java.util.Iterator;

/**
 * A heuristic that tries to resolve issues for critical pairs where one fully abstracted HC is a single nonterminal.
 *
 * - If both fully abstractes HCs are newly introduced nonterminals of the same rank -> Try to join them
 * - If the other HC is a "larger" HC try to add a rule from the nonterminal to this HC
 *
 */
public class SingleNonterminalHeuristic implements CompletionHeuristic {
    @Override
    public Iterable<CompletionState> applyHeuristic(CompletionState state) {
        return new Iterable<CompletionState>() {
            @Override
            public Iterator<CompletionState> iterator() {
                Iterator<CriticalPair> criticalPairIterator = state.getCriticalPairs().iterator();

                return new SimpleIterator<CompletionState>() {
                    @Override
                    public CompletionState computeNext() {
                        while (criticalPairIterator.hasNext()) {
                            NamedGrammar newGrammar = fixCriticalPair(state, criticalPairIterator.next());
                            if (newGrammar != null) {
                                // The current critical pair can be fixed -> Compute the following completion state
                                return new CompletionState(newGrammar);
                            }
                        }
                        return null;
                    }
                };
            }
        };
    }

    /**
     * @return The grammar that fixes the criticalPair, or null if this heuristic cannot fix the criticalPair
     */
    private Iterable<NamedGrammar> fixCriticalPair(CompletionState state, CriticalPair criticalPair) {
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();
        Nonterminal nt1 = getSingleUncollapsedNonterminal(hc1);
        Nonterminal nt2 = getSingleUncollapsedNonterminal(hc2);
        if (nt1 == null && nt2 == null) {
            return Collections.emptySet();
        }

        if (nt1 instanceof GeneratedNonterminal && nt2 instanceof GeneratedNonterminal && nt1.getRank() == nt2.getRank()) {
            // Check if critical pair are just two GeneratedNonterminals of the same rank
            return Collections.singleton(joinGeneratedNonterminals(state, (GeneratedNonterminal) nt1, (GeneratedNonterminal) nt2));
        } else {
            // Check if the right side is growing


        }

        // 2. Check if there is
        return null;
    }

    private NamedGrammar joinGeneratedNonterminals(CompletionState state, GeneratedNonterminal nt1, GeneratedNonterminal nt2) {
        // TODO: Implement
        throw new IllegalStateException();
    }

    /**
     * If hc consists of just a single uncollapsed nonterminal that nonterminal is returned. Otherwise null is returned.
     */
    private Nonterminal getSingleUncollapsedNonterminal(HeapConfiguration hc) {
        if (hc.nonterminalEdges().size() != 1) {
            return null;
        } else {
            // Check that there are no selectors
            TIntArrayList nodes = hc.nodes();
            for (int i = 0; i < nodes.size(); i++) {
                if (hc.selectorLabelsOf(nodes.get(i)).size() > 0) {
                    return null;
                }
            }

            // Check that the number of nodes matches the nonterminal rank
            Nonterminal nt = hc.labelOf(hc.nonterminalEdges().get(0));
            if (nodes.size() != nt.getRank()) {
                return null;
            } else {
                return nt;
            }
        }
    }
}
