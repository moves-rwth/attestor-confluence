package de.rwth.i2.attestor.grammar.confluence.jointMorphism;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.main.scene.SceneObject;
import org.junit.Before;
import org.junit.Test;

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
 */

public class EdgeOverlappingTest {

    private SceneObject sceneObject;
    private ExampleHcImplFactory hcImplFactory;
    private EdgeOverlapping baseOverlapping;

    @Before
    public void setUp() {
        sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
        HeapConfiguration hc1 = hcImplFactory.getListRule1();
        HeapConfiguration hc2 = hcImplFactory.getListRule2();
        HeapConfigurationContext context = new HeapConfigurationContext(hc1, hc2);
        baseOverlapping = EdgeOverlapping.getEdgeOverlapping(context);
        // TODO
    }

    @Test
    public void testNextPairCompatible() {
        // TODO
    }

    @Test
    public void testGetNextNodeOverlapping() {
        // TODO
    }

    @Test
    public void testAllNextEquivalences() {
        // TODO
    }

    @Test
    public void testIsEdgeOverlappingValid() {
        // TODO
    }

}
