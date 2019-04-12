package de.rwth.i2.attestor.grammar.typedness;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;

import java.util.*;

/**
 * A class that stores for each non terminal in the grammar, which outgoing selectors can be generated at each outgoing selector
 */
public class GrammarTypedness {
    private final Grammar grammar;
    private final Map<Pair<Nonterminal, Integer>, TentacleType> types;


    public GrammarTypedness(Grammar grammar) {
        this.grammar = grammar;
        this.types = new HashMap<>();
    }

    public Grammar getGrammar() {
        return grammar;
    }

    public TentacleType getTentacleType(Nonterminal nt, int tentacle) {
        return types.computeIfAbsent(new Pair<>(nt, tentacle), key -> new TentacleType(this, nt, tentacle));
    }

    public Set<SelectorLabel> getTypesAtNode(HeapConfiguration hc, int node) {
        Set<SelectorLabel> result = new HashSet<>();
        // Add immediate selectors
        result.addAll(hc.selectorLabelsOf(node));

        // Add recursive selectors
        for (Pair<Nonterminal, Integer> ntTentacle : GrammarTypedness.getConnectedTentacles(hc, node)) {
            Set<SelectorLabel> recursiveSelectors = getTentacleType(ntTentacle.first(), ntTentacle.second()).getAllTypes();
            result.addAll(recursiveSelectors);
        }

        return result;
    }

    // TODO: Might move this method in some util class (or directly to HeapConfiguration)
    public static Collection<Pair<Nonterminal, Integer>> getConnectedTentacles(HeapConfiguration hc, int node) {
        Collection<Pair<Nonterminal, Integer>> result = new ArrayList<>();

        hc.attachedNonterminalEdgesOf(node).forEach(ntEdge-> {
            Nonterminal nt = hc.labelOf(ntEdge);
            TIntArrayList attachedNodes = hc.attachedNodesOf(ntEdge);
            int offset = 0;
            while (offset != -1) {
                offset = attachedNodes.indexOf(offset, node);
                if (offset != -1) {
                    // attachedNodes[offset] == node
                    result.add(new Pair<>(nt, offset));
                    offset++;  // Search following indices
                }
            }
            return true;
        });

        return result;
    }

}
