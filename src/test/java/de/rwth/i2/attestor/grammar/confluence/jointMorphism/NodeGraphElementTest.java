package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.graph.morphism.Graph;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.*;

public class NodeGraphElementTest {


    private ExampleHcImplFactory hcImplFactory;

    @Before
    public void setUp() {
        SceneObject sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

    @Test
    public void testGetNodes() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodes = new TIntArrayList(3);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 3, nodes)
                .build();
        NodeGraphElement[] graphNodes = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        Collection<NodeGraphElement> excludeNodes = new HashSet<>();
        excludeNodes.add(graphNodes[0]);
        Set<NodeGraphElement> correctResult = new HashSet<>();
        correctResult.add(graphNodes[1]);
        correctResult.add(graphNodes[2]);

        // 2. Call getNodes and convert to set for easy equality check
        Set<NodeGraphElement> getNodesResult = new HashSet<>(NodeGraphElement.getNodes((Graph) hc, excludeNodes));
        assertEquals(correctResult, getNodesResult);
    }

    @Test
    public void testHasConnectedEdge_ConnectedNonterminalEdge() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test", 2, new boolean[]{false, false});

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodes)
                .addNonterminalEdge(nonterminal, nodes)
                .build();
        NodeGraphElement[] graphNodes = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        // 2. Call hasConnectedEdges
        assertTrue(graphNodes[0].hasConnectedEdges((Graph) hc));
    }

    @Test
    public void testHasConnectedEdge_SelectorPointingToNode() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodes)
                .addSelector(nodes.get(0), selectorLabel, nodes.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        // 2. Call hasConnectedEdges
        assertTrue(graphNodesHc1[1].hasConnectedEdges((Graph) hc));
    }

    @Test
    public void testHasConnectedEdge_SelectorPointingFromNode() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");

        TIntArrayList nodes = new TIntArrayList(2);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 2, nodes)
                .addSelector(nodes.get(0), selectorLabel, nodes.get(1))
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        // 2. Call hasConnectedEdges
        assertTrue(graphNodesHc1[0].hasConnectedEdges((Graph) hc));
    }

    @Test
    public void testHasConnectedEdge_NoConnectedEdge() {
        // 1. Setup the test
        Type type = hcImplFactory.scene().getType("node");
        SelectorLabel selectorLabel = hcImplFactory.scene().getSelectorLabel("test");

        TIntArrayList nodes = new TIntArrayList(1);
        HeapConfiguration hc = hcImplFactory.getEmptyHc().builder()
                .addNodes(type, 1, nodes)
                .build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc, nodes);

        // 2. Call hasConnectedEdges
        assertFalse(graphNodesHc1[0].hasConnectedEdges((Graph) hc));
    }
}
