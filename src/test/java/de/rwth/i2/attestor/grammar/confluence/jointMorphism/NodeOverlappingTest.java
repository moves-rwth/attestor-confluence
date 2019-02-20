package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import javax.xml.soap.Node;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


/**
 * Test cases:
 *
 * isNextPairCompatible:
 *   - Node types do not match
 *   - Added equivalence is violation point
 *   - Compatible nodes
 *     - Node is external in other graph
 *     - Node is internal in other graph (but not connected to any edges)
 *
 * isNodeOverlappingIndependent:
 *   - Independent overlapping
 *   - Not independent overlapping
 *
 * getOverlapping:
 *
 *
 */
public class NodeOverlappingTest {

    private ExampleHcImplFactory hcImplFactory;

    @Before
    public void setUp() {
        SceneObject sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

    @Test
    public void testIsNextPairCompatible_NonMatchingNodeTypes() {
        // 1. Setup the test
        Type type1 = hcImplFactory.scene().getType("node1");
        Type type2 = hcImplFactory.scene().getType("node2");
        TIntArrayList nodesHc1 = new TIntArrayList(1);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type1, 1, nodesHc1)
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(1);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type2, 1, nodesHc2)
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);

        // 2. Invoke method
        assertFalse(nodeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNextPairCompatible_NewPairIsViolationPoint() {
        // The new pair should be connected connected to an edge that is not in the intersection
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selectorLabel, nodesHc1.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selectorLabel, nodesHc2.get(1))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);

        // 2. Invoke method
        assertFalse(nodeOverlapping.isNextPairCompatible(newPair));
    }


    @Test
    public void testIsNextPairCompatible_CompatibleNodesExternal() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selectorLabel, nodesHc1.get(1))
                .setExternal(nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selectorLabel, nodesHc2.get(1))
                .setExternal(nodesHc2.get(0))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);

        // 2. Invoke method
        assertTrue(nodeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNextPairCompatible_CompatibleNodesInternal() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(1);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodesHc1)
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(1);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodesHc2)
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);

        // 2. Invoke method
        assertTrue(nodeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNodeOverlappingIndependent_True() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(1);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodesHc1)
                .setExternal(nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(1);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodesHc2)
                .setExternal(nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);
        NodeOverlapping newNodeOverlapping = nodeOverlapping.getOverlapping(newPair);

        // 2. Invoke method
        assertTrue(newNodeOverlapping.isNodeOverlappingIndependent());
    }

    @Test
    public void testIsNodeOverlappingIndependent_False() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");
        TIntArrayList nodesHc1 = new TIntArrayList(1);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodesHc1)
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(1);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodesHc2)
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);
        NodeOverlapping newNodeOverlapping = nodeOverlapping.getOverlapping(newPair);

        // 2. Invoke method
        assertFalse(newNodeOverlapping.isNodeOverlappingIndependent());
    }

    @Test
    public void testIsNodeOverlappingIdependent_ChildOfNotIndependentNodeOverlapping() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selectorLabel, nodesHc1.get(0))
                .setExternal(nodesHc1.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selectorLabel, nodesHc2.get(0))
                .setExternal(nodesHc2.get(1))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeGraphElement edge1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> newEdgePair = new Pair<>(edge1, edge2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context).getOverlapping(newEdgePair);
        Pair<NodeGraphElement, NodeGraphElement> newPair = new Pair<>(graphNodesHc1[0], graphNodesHc2[0]);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping).getOverlapping(newPair);
        assertFalse(nodeOverlapping.isNodeOverlappingIndependent()); // This node overlapping is not independent
        Pair<NodeGraphElement, NodeGraphElement> newPair2 = new Pair<>(graphNodesHc1[1], graphNodesHc2[1]);
        NodeOverlapping newNodeOverlapping = nodeOverlapping.getOverlapping(newPair2);

        // 2. Invoke method
        // The node overlapping should not be independent even though the new pair nodes are external
        assertFalse(newNodeOverlapping.isNodeOverlappingIndependent());
    }

}
