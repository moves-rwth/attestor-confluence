package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.grammar.CollapsedHeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.morphism.Graph;
import gnu.trove.list.array.TIntArrayList;

/**
 * A class to aggregate two HeapConfigurations that also implement the Graph interface.
 */
public class HeapConfigurationContext {
    private final CollapsedHeapConfiguration hc1, hc2;

    /**
     * @param hc1 The first HeapConfiguration (the object class must implement the Graph interface)
     * @param hc2 The second HeapConfiguration (the object class must implement the Graph interface)
     */
    public HeapConfigurationContext(HeapConfiguration hc1, HeapConfiguration hc2) {
        if (!(hc1 instanceof Graph) || !(hc2 instanceof Graph)) {
            throw new IllegalArgumentException("The given HeapConfiguration objects must also implement the Graph interface");
        }
        this.hc1 = convertToCollapsedHeapConfiguration(hc1);
        this.hc2 = convertToCollapsedHeapConfiguration(hc2);
    }

    public HeapConfigurationContext(CollapsedHeapConfiguration hc1, CollapsedHeapConfiguration hc2) {
        if (!(hc1.getCollapsed() instanceof Graph) || !(hc2.getCollapsed() instanceof Graph)) {
            throw new IllegalArgumentException("The given HeapConfiguration objects must also implement the Graph interface");
        }
        this.hc1 = hc1;
        this.hc2 = hc2;
    }

    public static CollapsedHeapConfiguration convertToCollapsedHeapConfiguration(HeapConfiguration hc) {
        // Set originalToCollapsedExternalIndices to null to detect that this is from the original rule
        // TODO: There might be a nicer solution
        return new CollapsedHeapConfiguration(hc, hc, null);
    }

    public HeapConfiguration getHc1() {
        return hc1.getCollapsed();
    }

    public HeapConfiguration getHc2() {
        return hc2.getCollapsed();
    }

    public CollapsedHeapConfiguration getCollapsedHc1() {
        return hc1;
    }

    public CollapsedHeapConfiguration getCollapsedHc2() {
        return hc2;
    }

    public boolean isHc1OriginalRule() {
        return hc1.getOriginalToCollapsedExternalIndices() == null;
    }

    public boolean isHc2OriginalRule() {
        return hc2.getOriginalToCollapsedExternalIndices() == null;
    }

    public Graph getGraph1() {
        return (Graph) hc1.getCollapsed();
    }

    public Graph getGraph2() {
        return (Graph) hc2.getCollapsed();
    }
}
