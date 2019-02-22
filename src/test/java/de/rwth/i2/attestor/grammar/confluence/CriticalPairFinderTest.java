package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeGraphElement;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.EdgeOverlapping;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.HeapConfigurationContext;
import de.rwth.i2.attestor.grammar.confluence.jointMorphism.Overlapping;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.programState.indexedState.BalancedTreeGrammar;
import de.rwth.i2.attestor.types.Type;
import org.junit.Before;
import org.junit.Test;

import java.util.Iterator;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class CriticalPairFinderTest {


    private ExampleHcImplFactory hcImplFactory;
    private SceneObject sceneObject;

    @Before
    public void setUp() {
        sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

    @Test
    public void testPossibleCriticalPairs() {
        // TODO
        Grammar balancedTreeGrammar = new BalancedTreeGrammar(sceneObject).getGrammar();
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(balancedTreeGrammar);
        System.err.println(criticalPairFinder.getCriticalPairs().size());
    }

}
