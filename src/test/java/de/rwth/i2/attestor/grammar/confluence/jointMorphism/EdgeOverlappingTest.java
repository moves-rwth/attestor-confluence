package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import static org.junit.Assert.fail;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

/**
 * Cases that are tested:
 *
 * getAllNextEquivalences()
 *    - 1. Case: Overlapping where there are no equivalences possible -> Throws exception
 *    - 2. Case: Overlapping with no present equivalences (repeated calls to getNextEquivalence())
 *    - 3. Case: Any overlapping with the following different edge compatibilities
 *        - 3.1 Case: Matching Selector edge in both graphs
 *        - 3.2 Case: Not Matching Selector edge in both graphs
 *        - 3.3 Case: Matching nonterminal edge in both graphs
 *        - 3.4 Case: Not Matching nonterminal edge in both graphs
 *        - 3.5 Case: Selector edge in one graph nonterminal in other graph
 *        - 3.6 Case: Matching edges
 *            - 3.6.1 Case: Connected node is in intersection and there is no violation
 *            - 3.6.2 Case: Connected node (in hc1) is in intersection, but does not match the correct node in hc2
 *            - 3.6.3 Case: Connected node (in hc2) is in intersection, but the connected node in hc1 is not in the intersection
 *
 *  isEdgeOverlappingValid()
 *     - 1. Case: There is an edge not in the intersection with a connected node not in the intersection
 *     - 2. Case: There is an edge not in the intersection with a connected node in the intersection
 *        - 2.1 Case: The node is external in the other graph
 *        - 2.1 Case: The node is not external in the other graph
 *
 *  TODO: Some kind of self loop test
 *  TODO: Test that the types of implied node overlappings are compatible
 */

public class EdgeOverlappingTest {

    private SceneObject sceneObject;
    private ExampleHcImplFactory hcImplFactory;
    private EdgeOverlapping baseOverlapping;

    @Before
    public void setUp() {
        sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

    /**
     * Test the getAllNextEquivalences() method where one of the graphs is empty. This case should raise an exception.
     */
    @Test
    public void testGetAllNextEquivalences_EmptyOverlapping() {
        // Setup an overlapping with empty graphs
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc();
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        try {
            edgeOverlapping.getAllNextEquivalences();
            fail("Expected an Exception to be thrown");
        } catch (Exception e) {

        }
    }

    @Test
    public void testGetAllNextEquivalences_MatchingSelectorEdges() {
        // 1. Setup the test
        SelectorLabel sel = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), sel, nodesHc1.get(1))
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(4);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc2)
                .addSelector(nodesHc2.get(2), sel, nodesHc2.get(3))
                .build();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);

        // 2. Invoke method
        Collection<Overlapping<EdgeGraphElement>> result = edgeOverlapping.getAllNextEquivalences();

        // 3. Basic checks
        assertEquals(1, result.size());
        Overlapping nextOverlapping = result.iterator().next();
        assertTrue(nextOverlapping instanceof EdgeOverlapping);
        EdgeOverlapping nextEdgeOverlapping = (EdgeOverlapping) nextOverlapping;

        // 4. Test if nodes map correctly
        NodeGraphElement node0Hc1 = new NodeGraphElement(nodesHc1.get(0));
        NodeGraphElement node0Hc2 = new NodeGraphElement(nodesHc2.get(2));
        NodeGraphElement node1Hc1 = new NodeGraphElement(nodesHc1.get(1));
        NodeGraphElement node1Hc2 = new NodeGraphElement(nodesHc2.get(3));
        assertEquals(node0Hc1, nextEdgeOverlapping.getHC1Node(node0Hc2));
        assertEquals(node0Hc2, nextEdgeOverlapping.getHC2Node(node0Hc1));
        assertEquals(node1Hc1, nextEdgeOverlapping.getHC1Node(node1Hc2));
        assertEquals(node1Hc2, nextEdgeOverlapping.getHC2Node(node1Hc1));

        // 5. Test if edges map correctly
        EdgeGraphElement edgeHc1 = new EdgeGraphElement(nodesHc1.get(0), "test");
        EdgeGraphElement edgeHc2 = new EdgeGraphElement(nodesHc2.get(2), "test");
        assertEquals(edgeHc1, nextEdgeOverlapping.getHC1Element(edgeHc2));
        assertEquals(edgeHc2, nextEdgeOverlapping.getHC2Element(edgeHc1));
    }

    @Test
    public void testGetAllNextEquivalences_NonMatchingSelectorEdges() {
        // 1. Setup the test
        SelectorLabel sel1 = hcImplFactory.scene().getSelectorLabel("test1");
        SelectorLabel sel2 = hcImplFactory.scene().getSelectorLabel("test2");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), sel1, nodesHc1.get(1))
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(4);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc2)
                .addSelector(nodesHc2.get(2), sel2, nodesHc2.get(3))
                .build();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);

        // 2. Invoke method
        Collection<Overlapping<EdgeGraphElement>> result = edgeOverlapping.getAllNextEquivalences();

        // 3. Check that there are no possible edge overlappings
        assertEquals(0, result.size());
    }

    @Test
    public void testGetAllNextEquivalences_MatchingNonterminalEdges() {
        // 1. Setup the test
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test", 2, new boolean[]{false, false});
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addNonterminalEdge(nonterminal, nodesHc1)
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(4);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc2)
                .addNonterminalEdge(nonterminal).addTentacle(nodesHc2.get(2)).addTentacle(nodesHc2.get(3)).build()
                .build();

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);

        // 2. Invoke method
        Collection<Overlapping<EdgeGraphElement>> result = edgeOverlapping.getAllNextEquivalences();

        // 3. Basic checks
        assertEquals(1, result.size());
        Overlapping nextOverlapping = result.iterator().next();
        assertTrue(nextOverlapping instanceof EdgeOverlapping);
        EdgeOverlapping nextEdgeOverlapping = (EdgeOverlapping) nextOverlapping;

        // 4. Test if nodes map correctly
        NodeGraphElement node0Hc1 = new NodeGraphElement(nodesHc1.get(0));
        NodeGraphElement node0Hc2 = new NodeGraphElement(nodesHc2.get(2));
        NodeGraphElement node1Hc1 = new NodeGraphElement(nodesHc1.get(1));
        NodeGraphElement node1Hc2 = new NodeGraphElement(nodesHc2.get(3));
        assertEquals(node0Hc1, nextEdgeOverlapping.getHC1Node(node0Hc2));
        assertEquals(node0Hc2, nextEdgeOverlapping.getHC2Node(node0Hc1));
        assertEquals(node1Hc1, nextEdgeOverlapping.getHC1Node(node1Hc2));
        assertEquals(node1Hc2, nextEdgeOverlapping.getHC2Node(node1Hc1));

        // 5. Test if edges map correctly
        EdgeGraphElement edgeHc1 = EdgeGraphElement.getEdgesOfGraph(context.getGraph1()).iterator().next();
        EdgeGraphElement edgeHc2 = EdgeGraphElement.getEdgesOfGraph(context.getGraph2()).iterator().next();
        assertEquals(edgeHc1, nextEdgeOverlapping.getHC1Element(edgeHc2));
        assertEquals(edgeHc2, nextEdgeOverlapping.getHC2Element(edgeHc1));
    }

    @Test
    public void testGetAllNextEquivalences_NonMatchingNonterminalEdges() {
        // TODO
    }

    @Test
    public void testGetAllNextEquivalences_SelectorInOneGraphNonterminalInOtherGraph() {
        // TODO
    }

    @Test
    public void testGetAllNextEquivalences_MatchingEdgeNoViolationWithNodeInIntersection() {
        // TODO
    }

    /**
     * Test the getAllNextEquivalences() method where two edges match and a connected node in HC1 is in the intersection,
     * but it maps not to the correct connected node in HC2.
     */
    @Test
    public void testGetAllNextEquivalences_MatchingEdgesViolationHC1Intersection() {
        // TODO
    }

    /**
     * Test the getAllNextEquivalences() method where two edges match and a connected node in HC2 is in the intersection,
     * but it maps not to the correct connected node in HC1.
     */
    @Test
    public void testGetAllNextEquivalences_MatchingEdgesViolationHC2Intersection() {
        // TODO
    }

    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is not in the intersection
     * and it is only connected to nodes that are not in the intersection. This case should be valid.
     */
    @Test
    public void testIsEdgeOverlappingValid_NonIntersectionNodeConnection() {
        // TODO
    }

    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is not in the intersection
     * and it is only connected to nodes in the intersection that are external in the other graph.
     * This case should be valid.
     */
    @Test
    public void testIsEdgeOverlappingValid_ExternalIntersectionNodeConnection() {
        // TODO
    }

    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is not in the intersection
     * and it is connected to a node in the intersection that is internal in the other graph.
     * This case should be invalid.
     */
    @Test
    public void testIsEdgeOverlappingValid_InternalIntersectionNodeConnection() {
        // TODO
    }

    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is in the intersection.
     * This case should be valid.
     */
    @Test
    public void testIsEdgeOverlappingValid_EdgeInIntersection() {
        // TODO
    }

}
