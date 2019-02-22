package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.graph.heap.internal.HeapConfigurationIdConverter;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.util.Pair;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class JointHeapConfigurationTest {

    private ExampleHcImplFactory hcImplFactory;

    @Before
    public void setUp() {
        SceneObject sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }


    /**
     * Tests a basic joint heap configuration
     */
    @Test
    public void testJointHeapConfigurationCreation() {
        // 1. Setup test
        SelectorLabel selector = hcImplFactory.scene().getSelectorLabel("test1");
        Nonterminal nonterminal = hcImplFactory.scene().createNonterminal("test2", 2, new boolean[]{false, false});
        Type type = hcImplFactory.scene().getType("node");

        TIntArrayList nodesHc1 = new TIntArrayList(3);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc(); // TODO: Add selectors & make external
        HeapConfigurationBuilder builder = hc1.builder()
                .addNodes(type, 3, nodesHc1);
        int nonterminalPublicId = builder.addNonterminalEdgeAndReturnId(nonterminal, nodesHc1);
        EdgeGraphElement nonterminalEdge = new EdgeGraphElement(HeapConfigurationIdConverter.getGraphId(hc1, nonterminalPublicId), null);

        hc1 = builder.build();
        NodeGraphElement[] graphNodesHc1 = NodeGraphElement.getGraphElementsFromPublicIds(hc1, nodesHc1);

        TIntArrayList nodesHc2 = new TIntArrayList(3);
        HeapConfiguration hc2 = hcImplFactory.getEmptyHc().builder() // TODO: Add selectors & make external
                .addNodes(type, 3, nodesHc2)
                .build();
        // TODO: Add nonterminal edge
        NodeGraphElement[] graphNodesHc2 = NodeGraphElement.getGraphElementsFromPublicIds(hc2, nodesHc2);

        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        // TODO: Add equivalences to edge & node overlappings
        EdgeOverlapping edgeOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        NodeOverlapping nodeOverlapping = NodeOverlapping.getNodeOverlapping(edgeOverlapping);
        JointHeapConfiguration jointHeapConfiguration = new JointHeapConfiguration(context, nodeOverlapping, edgeOverlapping);

        // 2. Check that the jointHeapConfiguration is correct
        HeapConfiguration jointHC = jointHeapConfiguration.getHeapConfiguration();

        // 2.1 Check that number nodes is correct
        assertEquals(0, jointHC.nodes().size()); // TODO

        // 2.2 Check that the number nonterminal edges is correct
        assertEquals(0, jointHC.nonterminalEdges().size());  // TODO

        // 3. Check that the matchings are correct (on some samples)
        // TODO
    }
}
