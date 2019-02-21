package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.*;

/**
 * Cases that are tested:
 *
 * iterator()
 *   - 1 Case: Empty overlapping
 *   - 2 Case: Non empty overlapping TODO
 *
 * isNextPairCompatible()
 *   - 1 Case: Matching Selector edge in both graphs
 *   - 2 Case: Not Matching Selector edge in both graphs
 *   - 3 Case: Matching nonterminal edge in both graphs
 *   - 4 Case: Not Matching nonterminal edge in both graphs
 *   - 5 Case: Selector edge in one graph nonterminal in other graph
 *   - 6 Case: Matching edges
 *     - 6.1 Case: Connected node already in intersection and there is no violation
 *     - 6.2 Case: Connected node (in hc1) is in intersection, but does not match the correct node in hc2
 *     - 6.3 Case: Connected node (in hc2) is in intersection, but the connected node in hc1 is not in the intersection
 *     - 6.4 Case: Connected nodes types do not match
 *
 *  isEdgeOverlappingValid()
 *    - 1. Case: There is an edge not in the intersection with a connected node not in the intersection
 *    - 2. Case: There is an edge not in the intersection with a connected node in the intersection
 *      - 2.1 Case: The node is external in the other graph
 *      - 2.1 Case: The node is not external in the other graph
 *
 *  getOverlapping(newPair)
 *    - Just add a pair and check that hc1Remaining, hc2Remaining, ... is correct
 *    - 1. Case: Add one pair
 *    - 1. Case: Add second pair
 *
 *  TODO: Some kind of test with self loops in the graph
 *  TODO: Test the getOverlapping method
 */
public class EdgeOverlappingTest {

    private ExampleHcImplFactory hcImplFactory;

    @Before
    public void setUp() {
        SceneObject sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

    @Test
    public void testIterator_EmptyOverlapping() {
        // Setup an overlapping with empty graphs
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc();
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        Iterator<Overlapping<EdgeGraphElement>> iterator = EdgeOverlapping.getEdgeOverlapping(context).iterator();
        assertTrue(iterator.hasNext());  // The iterator should return the base overlapping
        iterator.next();
        assertFalse(iterator.hasNext()); // Check that there are no additional overlappings
    }

    @Test
    public void testIsNextPairCompatible_MatchingSelectorEdges() {
        // 1. Setup the test
        SelectorLabel sel = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), sel, nodesHc1.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(4);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc2)
                .addSelector(nodesHc2.get(2), sel, nodesHc2.get(3))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edge1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge2 = graphNodesHc2[2].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> newPair = new Pair<>(edge1, edge2);

        // 2. Check isNextPairCompatible result
        assertTrue(edgeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNextPairCompatible_NonMatchingSelectorEdges() {
        // 1. Setup the test
        SelectorLabel sel1 = hcImplFactory.scene().getSelectorLabel("test1");
        SelectorLabel sel2 = hcImplFactory.scene().getSelectorLabel("test2");
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), sel1, nodesHc1.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(4);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc2)
                .addSelector(nodesHc2.get(2), sel2, nodesHc2.get(3))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edge1 = graphNodesHc1[0].getOutgoingSelectorEdge("test1");
        EdgeGraphElement edge2 = graphNodesHc2[2].getOutgoingSelectorEdge("test2");
        Pair<EdgeGraphElement, EdgeGraphElement> newPair = new Pair<>(edge1, edge2);

        // 2. Check isNextPairCompatible result
        assertFalse(edgeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNextPairCompatible_MatchingNonterminalEdges() {
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
        EdgeGraphElement edge1 = EdgeGraphElement.getEdgesOfGraph((Graph) hc1).iterator().next();
        EdgeGraphElement edge2 = EdgeGraphElement.getEdgesOfGraph((Graph) hc2).iterator().next();;
        Pair<EdgeGraphElement, EdgeGraphElement> newPair = new Pair<>(edge1, edge2);

        // 2. Check isNextPairCompatible result
        assertTrue(edgeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNextPairCompatible_NonMatchingNonterminalEdges() {
        // 1. Setup the test
        Nonterminal nonterminal1 = hcImplFactory.scene().createNonterminal("test1", 2, new boolean[]{false, false});
        Nonterminal nonterminal2 = hcImplFactory.scene().createNonterminal("test2", 2, new boolean[]{false, false});
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addNonterminalEdge(nonterminal1, nodesHc1)
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addNonterminalEdge(nonterminal2, nodesHc2)
                .build();

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edge1 = EdgeGraphElement.getEdgesOfGraph((Graph) hc1).iterator().next();
        EdgeGraphElement edge2 = EdgeGraphElement.getEdgesOfGraph((Graph) hc2).iterator().next();;
        Pair<EdgeGraphElement, EdgeGraphElement> newPair = new Pair<>(edge1, edge2);

        // 2. Check isNextPairCompatible result
        assertFalse(edgeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsNextPairCompatible_SelectorInOneGraphNonterminalInOtherGraph() {
        // 1. Setup the test
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test", 2, new boolean[]{false, false});
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addNonterminalEdge(nonterminal, nodesHc1)
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .build();

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edge1 = EdgeGraphElement.getEdgesOfGraph((Graph) hc1).iterator().next();
        EdgeGraphElement edge2 = EdgeGraphElement.getEdgesOfGraph((Graph) hc2).iterator().next();;
        Pair<EdgeGraphElement, EdgeGraphElement> newPair = new Pair<>(edge1, edge2);

        // 2. Check isNextPairCompatible result
        assertFalse(edgeOverlapping.isNextPairCompatible(newPair));
    }

    @Test
    public void testIsEdgeOverlappingValid_ConnectedNodesAlreadyInIntersection_NoViolation() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), selector, nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .addSelector(nodesHc2.get(1), selector, nodesHc2.get(0))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeGraphElement edge01Hc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge01Hc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> firstPair = new Pair<>(edge01Hc1, edge01Hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context).getOverlapping(firstPair);
        EdgeGraphElement edge10Hc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge10Hc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> newPair = new Pair<>(edge10Hc1, edge10Hc2);

        // 2. Check isEdgeOverlappingValid()
        assertTrue(edgeOverlapping.isNextPairCompatible(newPair));
    }

    /**
     * Test the getAllNextOverlappings() method where two edges match and a connected node in HC1 is in the intersection,
     * but it maps not to the correct connected node in HC2.
     */
    @Test
    public void testIsNextPairCompatible_MatchingEdgesViolationHC1Intersection() {
        // 1. Setup the test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(3);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 3, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), selector, nodesHc1.get(2))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(3);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 3, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .addSelector(nodesHc2.get(1), selector, nodesHc2.get(2))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        // Add equivalence of edges (0, 1) in hc1 and (1, 2) in hc2
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeGraphElement edge01Hc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge12Hc2 = graphNodesHc2[1].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> firstPair = new Pair<>(edge01Hc1, edge12Hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context).getOverlapping(firstPair);
        EdgeGraphElement edge12Hc1 = graphNodesHc1[1].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge01Hc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> secondPair = new Pair<>(edge12Hc1, edge01Hc2);

        // 2. Check isNextPairCompatible result
        assertFalse(edgeOverlapping.isNextPairCompatible(secondPair));
    }

    /**
     * Test the getAllNextOverlappings() method where two edges match and a connected node in HC2 is in the intersection,
     * but it maps not to the correct connected node in HC1.
     */
    @Test
    public void testIsNextPairCompatible_MatchingEdgesViolationHC2Intersection() {
        // 1. Setup the test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodesHc1 = new TIntArrayList(3);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 3, nodesHc1)
                .addSelector(nodesHc1.get(2), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), selector, nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(3);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 3, nodesHc2)
                .addSelector(nodesHc2.get(2), selector, nodesHc2.get(1))
                .addSelector(nodesHc2.get(1), selector, nodesHc2.get(0))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        // Add equivalence of edges (1, 0) in hc1 and (2, 1) in hc2
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeGraphElement edge10Hc1 = graphNodesHc1[1].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge21Hc2 = graphNodesHc2[2].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> firstPair = new Pair<>(edge10Hc1, edge21Hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context).getOverlapping(firstPair);
        EdgeGraphElement edge21Hc1 = graphNodesHc1[2].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge10Hc2 = graphNodesHc2[1].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> secondPair = new Pair<>(edge21Hc1, edge10Hc2);

        // 2. Check isNextPairCompatible result
        assertFalse(edgeOverlapping.isNextPairCompatible(secondPair));
    }

    @Test
    public void testIsNextPairCompatible_MatchingEdgesViolationNodeType() {
        // 1. Setup the test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type1 = hcImplFactory.scene().getType("node1");
        Type type2 = hcImplFactory.scene().getType("node2");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type1, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type1, 1, nodesHc2)
                .addNodes(type2, 1, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edgeHc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edgeHc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> firstPair = new Pair<>(edgeHc1, edgeHc2);

        // 2. Check isNextPairCompatible result
        assertFalse(edgeOverlapping.isNextPairCompatible(firstPair));
    }


    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is not in the intersection
     * and it is only connected to nodes that are not in the intersection. This case should be valid.
     */
    @Test
    public void testIsEdgeOverlappingValid_NonIntersectionNodeConnection() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(4);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .build();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);

        // 2. Check isEdgeOverlappingValid()
        assertTrue(edgeOverlapping.isEdgeOverlappingValid());
    }

    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is not in the intersection
     * and it is only connected to nodes in the intersection that are external in the other graph.
     * This case should be valid.
     */
    @Test
    public void testIsEdgeOverlappingValid_ExternalIntersectionNodeConnection() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), selector, nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .setExternal(nodesHc2.get(0)).setExternal(nodesHc2.get(1))
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeGraphElement edge1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context).getOverlapping(new Pair<>(edge1, edge2));

        // 2. Check isEdgeOverlappingValid()
        assertTrue(edgeOverlapping.isEdgeOverlappingValid());
    }

    /**
     * Tests the isEdgeOverlappingValid() method where the overlapping contains an edge that is not in the intersection
     * and it is connected to a node in the intersection that is internal in the other graph.
     * This case should be invalid.
     */
    @Test
    public void testIsEdgeOverlappingValid_InternalIntersectionNodeConnection() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), selector, nodesHc1.get(0))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .setExternal(nodesHc2.get(0))
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeGraphElement edge1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context).getOverlapping(new Pair<>(edge1, edge2));

        // 2. Check isEdgeOverlappingValid()
        assertFalse(edgeOverlapping.isEdgeOverlappingValid());
    }

    @Test
    public void testGetNextEquivalence_SameNumberEdges() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(4);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(2), selector, nodesHc1.get(3))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(4);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .addSelector(nodesHc2.get(2), selector, nodesHc2.get(3))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping baseOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edge0Hc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge1Hc1 = graphNodesHc1[2].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge0Hc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge1Hc2 = graphNodesHc2[2].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> edgePair00 = new Pair<>(edge0Hc1, edge0Hc2);
        Pair<EdgeGraphElement, EdgeGraphElement> edgePair01 = new Pair<>(edge0Hc1, edge1Hc2);
        Pair<EdgeGraphElement, EdgeGraphElement> edgePair10 = new Pair<>(edge1Hc1, edge0Hc2);
        Pair<EdgeGraphElement, EdgeGraphElement> edgePair11 = new Pair<>(edge1Hc1, edge1Hc2);


        // 2. Check the immediate successors of the base overlapping
        assertEquals(edgePair00, baseOverlapping.getNextEquivalence(null));
        assertEquals(edgePair01, baseOverlapping.getNextEquivalence(edgePair00));
        assertEquals(edgePair10, baseOverlapping.getNextEquivalence(edgePair01));
        assertEquals(edgePair11, baseOverlapping.getNextEquivalence(edgePair10));
        assertEquals(null, baseOverlapping.getNextEquivalence(edgePair11));

        // 3. Check all descendants
        // Add edgePair00 first
        EdgeOverlapping childOverlapping = baseOverlapping.getOverlapping(edgePair00);
        assertEquals(edgePair11, childOverlapping.getNextEquivalence(null));
        assertEquals(null, childOverlapping.getNextEquivalence(edgePair11));
        childOverlapping = childOverlapping.getOverlapping(edgePair11);
        assertEquals(null, childOverlapping.getNextEquivalence(null));

        // Add edgePair01 first
        childOverlapping = baseOverlapping.getOverlapping(edgePair01);
        assertEquals(edgePair10, childOverlapping.getNextEquivalence(null));
        assertEquals(null, childOverlapping.getNextEquivalence(edgePair10));
        childOverlapping = childOverlapping.getOverlapping(edgePair10);
        assertEquals(null, childOverlapping.getNextEquivalence(null));

        // Add edgePair10 first
        childOverlapping = baseOverlapping.getOverlapping(edgePair10);
        assertEquals(null, childOverlapping.getNextEquivalence(null));

        // Add edgePair11 first
        childOverlapping = baseOverlapping.getOverlapping(edgePair11);
        assertEquals(null, childOverlapping.getNextEquivalence(null));

    }


    /**
     * This test checks how the getAllNextOverlappings() method handles if there are a different number of edge
     */
    @Test
    public void testGetNextEquivalence_DifferentEdgeNumber() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test");
        Type type = hcImplFactory.scene().getType("node");
        TIntArrayList nodesHc1 = new TIntArrayList(4);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 4, nodesHc1)
                .addSelector(nodesHc1.get(0), selector, nodesHc1.get(1))
                .addSelector(nodesHc1.get(2), selector, nodesHc1.get(3))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(2);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodesHc2)
                .addSelector(nodesHc2.get(0), selector, nodesHc2.get(1))
                .build();
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        EdgeOverlapping baseOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        EdgeGraphElement edge0Hc1 = graphNodesHc1[0].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge1Hc1 = graphNodesHc1[2].getOutgoingSelectorEdge("test");
        EdgeGraphElement edge0Hc2 = graphNodesHc2[0].getOutgoingSelectorEdge("test");
        Pair<EdgeGraphElement, EdgeGraphElement> edgePair00 = new Pair<>(edge0Hc1, edge0Hc2);
        Pair<EdgeGraphElement, EdgeGraphElement> edgePair10 = new Pair<>(edge1Hc1, edge0Hc2);

        // 2. Check the immediate successors of the base overlapping
        assertEquals(edgePair00, baseOverlapping.getNextEquivalence(null));
        assertEquals(edgePair10, baseOverlapping.getNextEquivalence(edgePair00));
        assertEquals(null, baseOverlapping.getNextEquivalence(edgePair10));
    }

}
