package de.rwth.i2.attestor.io.tikzOutput;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.CriticalPair;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeGraphElement;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.NodeGraphElement;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;

import java.util.Collection;

public class TikzExport {
    private String destinationLocation;

    public TikzExport(String destinationLocation) {
        this.destinationLocation = destinationLocation;
        // TODO: Copy default preamble to destination location
    }

    public void exportCriticalPairs(Collection<CriticalPair> criticalPairs) {
        // TODO
    }

    public void exportGrammar(Grammar grammar) {
        // TODO
    }

    private StringBuilder exportHeapConfiguration(HeapConfiguration hc) {
        // TODO
        return null;
    }

    private StringBuilder exportNode(HeapConfiguration hc, NodeGraphElement nodeGraphElement) {
        // TODO
        return null;
    }

    private StringBuilder exportEdge(HeapConfiguration hc, EdgeGraphElement selectorEdge) {
        // TODO
        return null;
    }

    private StringBuilder exportGrammarRule(Nonterminal nonterminal, CollapsedHeapConfiguration rightHandSide) {
        // TODO
        return null;
    }

}
