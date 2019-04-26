package de.rwth.i2.attestor.grammar.confluence.completion.heuristics;

import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.completion.CompletionState;
import de.rwth.i2.attestor.grammar.confluence.completion.GeneratedNonterminal;
import de.rwth.i2.attestor.grammar.util.SimpleIterator;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import gnu.trove.list.array.TIntArrayList;

import java.util.Iterator;

/**
 * If both fully abstracted HCs are handles of newly introduced nonterminals of the same rank -> Try to join them
 *
 */
public class JoinGeneratedNonterminalsHeuristic implements CompletionHeuristic {
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
    private NamedGrammar fixCriticalPair(CompletionState state, CriticalPair criticalPair) {
        HeapConfiguration hc1 = criticalPair.getCanonical1();
        HeapConfiguration hc2 = criticalPair.getCanonical2();
        GeneratedNonterminal nt1 = getSingleUncollapsedGeneratedNonterminal(hc1);
        GeneratedNonterminal nt2 = getSingleUncollapsedGeneratedNonterminal(hc2);

        if (nt1 != null && nt2 != null && nt1.getRank() == nt2.getRank()) {
            // Check if critical pair are just two GeneratedNonterminals of the same rank
            return state.getGrammar().joinGeneratedNonterminals((GeneratedNonterminal) nt1, (GeneratedNonterminal) nt2);
        } else {
            return null;
        }
    }

    /**
     * If hc consists of just a single uncollapsed nonterminal that nonterminal is returned. Otherwise null is returned.
     */
    private GeneratedNonterminal getSingleUncollapsedGeneratedNonterminal(HeapConfiguration hc) {
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
            if (nt instanceof GeneratedNonterminal && nodes.size() == nt.getRank()) {
                // The heap configuration is a handle of a generated
                return (GeneratedNonterminal) nt;
            } else {
                return null;
            }
        }
    }
}
