package de.rwth.i2.attestor.grammar.confluence;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.GrammarBuilder;
import de.rwth.i2.attestor.grammar.confluence.main.ConfluenceTool;
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

import java.util.Collection;

import static org.junit.Assert.assertEquals;


public class CriticalPairFinderTest {

    @Before
    public void setUp() {
        sceneObject = new MockupSceneObject();
        hcImplFactory = new ExampleHcImplFactory(sceneObject);
    }

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
        assertEquals(CriticalPair.Joinability.NOT_JOINABLE, criticalPairFinder.getJoinabilityResult());
        Collection<CriticalPair> criticalPairs = criticalPairFinder.getCriticalPairs();
        assertEquals(3, criticalPairs.size());
    }


    @Test
    public void testPossibleCriticalPairs() {
        Grammar balancedTreeGrammar = new BalancedTreeGrammar(sceneObject).getGrammar();
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(balancedTreeGrammar);
        System.err.println(criticalPairFinder.getCriticalPairs().size());
        // TODO: Add assert
    }

    @Test
    public void testDefaultGrammar_BT_conf() {
        testGrammar("BT_conf", CriticalPair.Joinability.STRONGLY_JOINABLE);
    }

    @Test
    public void testDefaultGrammar_BT() {
        testGrammar("BT", CriticalPair.Joinability.STRONGLY_JOINABLE);
    }

    @Test
    public void testDefaultGrammar_DLList() {
        testGrammar("DLList", CriticalPair.Joinability.STRONGLY_JOINABLE);
    }

    @Test
    public void testDefaultGrammar_SLList() {
        testGrammar("SLList", CriticalPair.Joinability.STRONGLY_JOINABLE);
    }


    public void testGrammar(String grammarName, CriticalPair.Joinability joinability) {
        Grammar grammar = ConfluenceTool.parseGrammar(grammarName);
        CriticalPairFinder criticalPairFinder = new CriticalPairFinder(grammar);
        // TODO: Assert joinability
        // assertEquals(joinability, criticalPairFinder.getJoinabilityResult());
    }

}
