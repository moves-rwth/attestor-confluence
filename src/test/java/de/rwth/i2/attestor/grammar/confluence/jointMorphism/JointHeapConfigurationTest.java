package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.Matching;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.graph.heap.internal.HeapConfigurationIdConverter;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class JointHeapConfigurationTest {

    private HeapConfigurationContext context;
    private NodeGraphElement[] graphNodesHc1;
    private NodeGraphElement[] graphNodesHc2;
    private EdgeGraphElement nonterminalEdgeHc1;
    private EdgeGraphElement nonterminalEdgeHc2;
    private EdgeGraphElement selectorEdgeHc1;
    private EdgeGraphElement selectorEdgeHc2;

    @Before
    public void setUp() {
        SceneObject sceneObject = new MockupSceneObject();
        ExampleHcImplFactory hcImplFactory = new ExampleHcImplFactory(sceneObject);

        // Create overlapping test graph (contains nonterminal of rank two and a selector in each graph)
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test1");
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test2", 2, new boolean[]{false, false});
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodesHc1 = new TIntArrayList(3);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc();
        HeapConfigurationBuilder builderHc1 = hc1.builder()
                .addNodes(type, 3, nodesHc1)
                .setExternal(nodesHc1.get(0))
                .addSelector(nodesHc1.get(1), selector, nodesHc1.get(0));
        TIntArrayList hc1ConnectedNodes = TIntArrayList.wrap(new int[]{nodesHc1.get(1), nodesHc1.get(2)});
        int nonterminalHc1PublicId = builderHc1.addNonterminalEdgeAndReturnId(nonterminal, hc1ConnectedNodes);
        nonterminalEdgeHc1 = new EdgeGraphElement(HeapConfigurationIdConverter.getGraphId(hc1, nonterminalHc1PublicId), null);
        hc1 = builderHc1.build();
        graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);
        selectorEdgeHc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test1");

        TIntArrayList nodesHc2 = new TIntArrayList(3);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc();
        HeapConfigurationBuilder builderHc2 = hc2.builder()
                .addNodes(type, 3, nodesHc2)
                .setExternal(nodesHc2.get(0))
                .addSelector(nodesHc2.get(2), selector, nodesHc2.get(0));
        TIntArrayList hc2ConnectedNodes = TIntArrayList.wrap(new int[]{nodesHc2.get(1), nodesHc2.get(2)});
        int nonterminalHc2PublicId = builderHc2.addNonterminalEdgeAndReturnId(nonterminal, hc2ConnectedNodes);
        nonterminalEdgeHc2 = new EdgeGraphElement(HeapConfigurationIdConverter.getGraphId(hc2, nonterminalHc2PublicId), null);
        hc2 = builderHc2.build();
        graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);
        selectorEdgeHc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test1");

        context = new HeapConfigurationContext(hc1, hc2);
    }

    @Test
    public void testJointHeapConfigurationCreation_OnlyNonterminalOverlap() {
        // 1. Setup the EdgeOverlapping & NodeOverlapping
        // Adding the nonterminal edge creates an implied node equivalence between the 1th and 2nd node of both HCs
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context)
                .getOverlapping(new Pair<>(nonterminalEdgeHc1, nonterminalEdgeHc2));
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        JointHeapConfiguration jointHeapConfiguration = new JointHeapConfiguration(edgeOverlapping, nodeOverlapping, null, null, null);
        HeapConfiguration jointHc = jointHeapConfiguration.getHeapConfiguration();
        Graph jointHcGraph = (Graph) jointHc;

        // 2. Check that the jointHeapConfiguration is correct

        // 2.1 Check that number nodes is correct
        assertEquals(4, jointHc.nodes().size());

        // 2.2 Check that the number nonterminal edges is correct
        assertEquals(1, jointHc.nonterminalEdges().size());

        // 3. Check that the matchings are correct (on some samples)
        Matching matching1 = jointHeapConfiguration.getMatching1();
        Matching matching2 = jointHeapConfiguration.getMatching2();

        int mappedNode0Hc1 = matching1.match(graphNodesHc1[0].getPrivateId());
        int mappedNode0Hc2 = matching2.match(graphNodesHc2[0].getPrivateId());
        int mappedNode1 = matching1.match(graphNodesHc1[1].getPrivateId());
        int mappedNode2 = matching1.match(graphNodesHc1[2].getPrivateId());
        int mappedNonTerminalEdge = matching1.match(nonterminalEdgeHc1.getPrivateId());

        // Check that the nonterminal edges maps to the same nonterminal edge
        assertEquals(mappedNonTerminalEdge, matching2.match(nonterminalEdgeHc2.getPrivateId()));

        // Check that the 1th node of both graphs map to the same node
        assertEquals(mappedNode1, matching2.match(graphNodesHc2[1].getPrivateId()));

        // Check that the 2nd node of both graphs map to the same node
        assertEquals(mappedNode2, matching2.match(graphNodesHc2[2].getPrivateId()));

        // Check that the hc1 selector is still present
        TIntArrayList correctSuccessorsMappedNode1 = TIntArrayList.wrap(new int[]{mappedNode0Hc1});
        assertEquals(correctSuccessorsMappedNode1, jointHcGraph.getSuccessorsOf(mappedNode1));

        // Check that the hc2 selector is still present
        TIntArrayList correctSuccessorsMappedNode2 = TIntArrayList.wrap(new int[]{mappedNode0Hc2});
        assertEquals(correctSuccessorsMappedNode2, jointHcGraph.getSuccessorsOf(mappedNode2));

        // Check that the nonterminal edge is still present
        TIntArrayList connectedNodesNonterminal = TIntArrayList.wrap(new int[]{mappedNode1, mappedNode2});
        assertEquals(connectedNodesNonterminal, jointHcGraph.getSuccessorsOf(mappedNonTerminalEdge));
    }


    @Test
    public void testJointHeapConfigurationCreation_AdditionalNodeOverlap() {
        // 1. Setup the EdgeOverlapping & NodeOverlapping
        // Adding the nonterminal edge creates an implied node equivalence between the 1th and 2nd node of both HCs
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context)
                .getOverlapping(new Pair<>(nonterminalEdgeHc1, nonterminalEdgeHc2));
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping)
                .getOverlapping(new Pair<>(graphNodesHc1[0], graphNodesHc2[0]));
        JointHeapConfiguration jointHeapConfiguration = new JointHeapConfiguration(edgeOverlapping, nodeOverlapping, null, null, null);
        HeapConfiguration jointHc = jointHeapConfiguration.getHeapConfiguration();
        Graph jointHcGraph = (Graph) jointHc;

        // 2. Check that the jointHeapConfiguration is correct

        // 2.1 Check that number nodes is correct
        assertEquals(3, jointHc.nodes().size());

        // 2.2 Check that the number nonterminal edges is correct
        assertEquals(1, jointHc.nonterminalEdges().size());

        // 3. Check that the matchings are correct (on some samples)
        Matching matching1 = jointHeapConfiguration.getMatching1();
        Matching matching2 = jointHeapConfiguration.getMatching2();

        int mappedNode0 = matching1.match(graphNodesHc1[0].getPrivateId());
        int mappedNode1 = matching1.match(graphNodesHc1[1].getPrivateId());
        int mappedNode2 = matching1.match(graphNodesHc1[2].getPrivateId());
        int mappedNonTerminalEdge = matching1.match(nonterminalEdgeHc1.getPrivateId());

        // Check that the nonterminal edges maps to the same nonterminal edge
        assertEquals(mappedNonTerminalEdge, matching2.match(nonterminalEdgeHc2.getPrivateId()));

        // Check that the 0th node of both graphs map to the same node
        assertEquals(mappedNode0, matching2.match(graphNodesHc2[0].getPrivateId()));

        // Check that the 1th node of both graphs map to the same node
        assertEquals(mappedNode1, matching2.match(graphNodesHc2[1].getPrivateId()));

        // Check that the 2nd node of both graphs map to the same node
        assertEquals(mappedNode2, matching2.match(graphNodesHc2[2].getPrivateId()));

        // Check that hc1 & hc2 selector is still present
        assertTrue(jointHcGraph.hasEdge(mappedNode1, mappedNode0));
        assertTrue(jointHcGraph.hasEdge(mappedNode2, mappedNode0));

        // Check that the nonterminal edge is still present
        TIntArrayList connectedNodesNonterminal = TIntArrayList.wrap(new int[]{mappedNode1, mappedNode2});
        assertEquals(connectedNodesNonterminal, jointHcGraph.getSuccessorsOf(mappedNonTerminalEdge));
    }
}
