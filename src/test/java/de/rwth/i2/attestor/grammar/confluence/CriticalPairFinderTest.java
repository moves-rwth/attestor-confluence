package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.HeapConfigurationBuilder;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.programState.indexedState.BalancedTreeGrammar;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertTrue;


public class CriticalPairFinderTest {


    private ExampleHcImplFactory hcImplFactory;
    private SceneObject sceneObject;

    private Grammar getSimpleDLLGrammar() {
        Nonterminal list = hcImplFactory.scene().createNonterminal("L", 2, new boolean[]{false, false});
        SelectorLabel nextPointer = hcImplFactory.scene().getSelectorLabel("n");
        SelectorLabel previousPointer = hcImplFactory.scene().getSelectorLabel("p");
        Type listElement = hcImplFactory.scene().getType("element");
        TIntArrayList nodesHc1 = new TIntArrayList(2);
        HeapConfiguration hc1 = hcImplFactory.getEmptyHc().builder()
                .addNodes(listElement, 2, nodesHc1)
                .setExternal(nodesHc1.get(0)).setExternal(nodesHc1.get(1))
                .addSelector(nodesHc1.get(0), nextPointer, nodesHc1.get(1))
                .addSelector(nodesHc1.get(1), previousPointer, nodesHc1.get(0))
                .build();

        TIntArrayList nodesHc2 = new TIntArrayList(3);
        HeapConfigurationBuilder hc2Builder = hcImplFactory.getEmptyHc().builder()
                .addNodes(listElement, 3, nodesHc2)
                .setExternal(nodesHc2.get(0)).setExternal(nodesHc2.get(2))
                .addSelector(nodesHc2.get(0), nextPointer, nodesHc2.get(1))
                .addSelector(nodesHc2.get(1), previousPointer, nodesHc2.get(0));

        int nonTerminalEdge = hc2Builder.addNonterminalEdgeAndReturnId(list, TIntArrayList.wrap(new int[] {nodesHc2.get(1), nodesHc2.get(2)}));
        HeapConfiguration hc2 = hc2Builder.build();

        return new GrammarBuilder()
                .addRule(list, hc1)
                .addRule(list, hc2)
                .updateCollapsedRules()
                .build();
    }

    @Test
    public void testSimpleDLLGrammar() {
        Grammar grammar = getSimpleDLLGrammar();
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        Set<CriticalPair> criticalPairs = criticalPairFinder.getCriticalPairs();
        assertTrue(true);
    }


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
