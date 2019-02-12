package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeJointMorphism;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.GraphElement;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.HeapConfigurationContext;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.NodeJointMorphism;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;

import java.util.HashMap;
import java.util.Map;

public class JointHeapConfiguration {

    /**
     * Creates a new HeapConfiguration that is the union between the two HeapConfigurations in the context object.
     * The overlapping is specified by nodeJointMorphism and edgeJointMorphism.
     */
    public static HeapConfiguration getHeapConfiguration(HeapConfigurationContext context,
                                                         NodeJointMorphism nodeJointMorphism,
                                                         EdgeJointMorphism edgeJointMorphism) {
        // Create a new HeapConfigurationBuilder (by using getEmpty() on one of the existing heap configurations
        //     we are independent of the InternalHeapConfiguration class)
        HeapConfigurationBuilder jointHeapConfigurationBuilder = context.getHc1().getEmpty().builder();

        // Create maps to keep track of which GraphElement maps to which public ID in the new HeapConfiguration
        Map<GraphElement, Integer> mapHC1, mapHC2;
        mapHC1 = new HashMap<>();
        mapHC2 = new HashMap<>();

        // Add nodes
        // TODO: Add nodes from both graphs

        // Add edges
        // TODO: Add edges from both graphs

        return jointHeapConfigurationBuilder.build();
    }
}
