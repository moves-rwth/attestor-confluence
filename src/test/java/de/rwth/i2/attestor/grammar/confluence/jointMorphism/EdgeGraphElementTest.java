package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.graph.heap.internal.HeapConfigurationIdConverter;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static junit.framework.TestCase.fail;
import static org.junit.Assert.assertEquals;

public class EdgeGraphElementTest {

    private ExampleHcImplFactory hcImplFactory;

    @Before
    public void setUp() {
        SceneObject sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

    @Test
    public void testGetConnectedNodes_Selector() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel1 = hcImplFactory.scene().getSelectorLabel("test1");
        SelectorLabel selectorLabel2 = hcImplFactory.scene().getSelectorLabel("test2");

        TIntArrayList nodes = new TIntArrayList(3);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 3, nodes)
                .addSelector(nodes.get(0), selectorLabel1, nodes.get(1))
                .addSelector(nodes.get(0), selectorLabel2, nodes.get(2))
                .build();
        NodeGraphElement[] graphNodes = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        List<NodeGraphElement> correctResult = new ArrayList<>();
        correctResult.add(graphNodes[0]);
        correctResult.add(graphNodes[2]);
        EdgeGraphElement edge = graphNodes[0].getOutgoingSelectorEdge("test2");

        // 2. Call getConnectedNodes
        assertEquals(correctResult, edge.getConnectedNodes((Graph) hc));
    }

    @Test
    public void testGetConnectedNodes_Nonterminal() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test", 2, new boolean[]{false, false});

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodes)
                .addNonterminalEdge(nonterminal, nodes)
                .build();
        NodeGraphElement[] graphNodes = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        List<NodeGraphElement> correctResult = new ArrayList<>();
        correctResult.add(graphNodes[0]);
        correctResult.add(graphNodes[1]);
        EdgeGraphElement edge = EdgeGraphElement.getEdgesOfGraph((Graph) hc).iterator().next();

        // 2. Call getConnectedNodes
        assertEquals(correctResult, edge.getConnectedNodes((Graph) hc));
    }

    @Test
    public void testGetConnectedNodes_InvalidSelector() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodes)
                .build();
        NodeGraphElement[] graphNodes = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        EdgeGraphElement edge = graphNodes[0].getOutgoingSelectorEdge("test"); // Edge not present in graph

        // 2. Call getConnectedNodes
        try {
            edge.getConnectedNodes((Graph) hc);
            fail("this should throw an error since the edge is not in the given graph.");
        } catch (IllegalArgumentException e) {
            // this is expected
        }
    }

    @Test
    public void testGetConnectedNodes_InvalidNonterminal() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodes)
                .build();

        EdgeGraphElement edge = new EdgeGraphElement(0, null);

        // 2. Call getConnectedNodes
        try {
            edge.getConnectedNodes((Graph) hc);
            fail("this should throw an error since the edge is not in the given graph.");
        } catch (IllegalArgumentException e) {
            // this is expected
        }
    }

    @Test
    public void testGetEdgesOfGraph() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test1");
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test2", 2, new boolean[]{false, false});

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc();
        HeapConfigurationBuilder builder = hc.builder()
                .addNodes(type, 2, nodes)
                .addSelector(nodes.get(0), selectorLabel, nodes.get(1));
        int nonterminalPublicId = builder.addNonterminalEdgeAndReturnId(nonterminal, nodes);
        EdgeGraphElement nonterminalEdge = new EdgeGraphElement(HeapConfigurationIdConverter.getGraphId(hc, nonterminalPublicId), null);
        hc = builder.build();  // This is probably not necessary
        NodeGraphElement[] graphNodes = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);
        EdgeGraphElement selectorEdge = graphNodes[0].getOutgoingSelectorEdge("test1");

        Set<EdgeGraphElement> correctResult = new HashSet<>();
        correctResult.add(nonterminalEdge);
        correctResult.add(selectorEdge);

        // 2. Call getEdgesOfGraph and cast to Set for easy equality check
        Set<EdgeGraphElement> testResult = new HashSet<>(EdgeGraphElement.getEdgesOfGraph((Graph) hc));
        assertEquals(correctResult, testResult);
    }
}
