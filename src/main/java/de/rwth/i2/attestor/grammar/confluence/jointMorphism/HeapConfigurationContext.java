package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;

import java.security.InvalidParameterException;

/**
 * A convinience class to aggregate two HeapConfigurations that also implement the Graph interface.
 */
public class HeapConfigurationContext {
    private final HeapConfiguration hc1, hc2;
    private final Graph graph1, graph2;

    /**
     * @param hc1 The first HeapConfiguration (the object class must implement the Graph interface)
     * @param hc1 The second HeapConfiguration (the object class must implement the Graph interface)
     */
    public HeapConfigurationContext(HeapConfiguration hc1, HeapConfiguration hc2) {
        if (!(hc1 instanceof Graph) || !(hc2 instanceof Graph)) {

            throw new IllegalArgumentException("The given HeapConfiguration objects must also implement the Graph interface");
        }
        this.hc1 = hc1;
        this.hc2 = hc2;
        this.graph1 = (Graph) hc1;
        this.graph2 = (Graph) hc2;
    }

    public HeapConfiguration getHc1() {
        return hc1;
    }

    public HeapConfiguration getHc2() {
        return hc2;
    }

    public Graph getGraph1() {
        return graph1;
    }

    public Graph getGraph2() {
        return graph2;
    }
}
