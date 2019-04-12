package de.rwth.i2.attestor.grammar.typedness;

import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.NamedGrammar;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.util.Pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
        // TODO: Implement
        throw new UnsupportedOperationException();
    }
}
