package de.rwth.i2.attestor.markingGeneration.visited;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.grammar.Grammar;
import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationStrategy;
import de.rwth.i2.attestor.grammar.canonicalization.CanonicalizationStrategyBuilder;
import de.rwth.i2.attestor.grammar.materialization.MaterializationStrategy;
import de.rwth.i2.attestor.grammar.materialization.MaterializationStrategyBuilder;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.ExampleHcImplFactory;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.phases.symbolicExecution.utilStrategies.NoAbortStrategy;
import de.rwth.i2.attestor.stateSpaceGeneration.ProgramState;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.Test;

import java.util.*;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class VisitedMarkingGeneratorTest {

    private SceneObject sceneObject;
    private Nonterminal nt;
    private Type type;
    private ExampleHcImplFactory hcFactory;

    @Before
    public void setUp() {

        sceneObject = new MockupSceneObject();
        hcFactory = new ExampleHcImplFactory(sceneObject);
        nt = sceneObject.scene().createNonterminal("List", 2, new boolean[]{false, true});
        type = sceneObject.scene().getType("List");


    }


    @Test
    public void testWithoutNonterminals() {

        VisitedMarkingGenerator generator = createGenerator("%x", Collections.singleton("next"));

        HeapConfiguration hc = hcFactory.getEmptyGraphWithConstants();
        ProgramState initialState = sceneObject.scene().createProgramState(hc);

        Collection<HeapConfiguration> result = generator.marked(initialState);
        assertTrue(result.isEmpty());

    }

    private VisitedMarkingGenerator createGenerator(String markingName, Collection<String> availableSelectors) {

        Grammar grammar = setupGrammar();

        MaterializationStrategy materializationStrategy = new MaterializationStrategyBuilder()
                .setGrammar(grammar)
                .setIndexedMode(false)
                .build();

        CanonicalizationStrategy canonicalizationStrategy = new CanonicalizationStrategyBuilder()
                .setAggressiveNullAbstraction(true)
                .setMinAbstractionDistance(0)
                .setGrammar(grammar)
                .build();

        return new VisitedMarkingGenerator(
                markingName,
                availableSelectors,
                new NoAbortStrategy(),
                materializationStrategy,
                canonicalizationStrategy
        );

    }

    private Grammar setupGrammar() {

        Map<Nonterminal, Set<HeapConfiguration>> rules = new LinkedHashMap<>();
        rules.put(nt, new LinkedHashSet<>());
        rules.get(nt).add(hcFactory.getListRule1());
        rules.get(nt).add(hcFactory.getListRule2());
        rules.get(nt).add(hcFactory.getListRule3());

        return new Grammar(rules);
    }

    @Test
    public void testWithNonterminals() {

        final String markingName = "%z";
        VisitedMarkingGenerator generator = createGenerator(markingName, Collections.singleton("next"));

        HeapConfiguration hc = hcFactory.getEmptyGraphWithConstants();
        TIntArrayList nodes = new TIntArrayList();

        hc = hc.builder()
                .addNodes(type, 2, nodes)
                .addNonterminalEdge(nt)
                .addTentacle(nodes.get(0))
                .addTentacle(nodes.get(1))
                .build()
                .build();

        Set<HeapConfiguration> expectedMarkedHcs = new LinkedHashSet<>();

        expectedMarkedHcs.add(hc.clone().builder()
                .addVariableEdge(markingName, nodes.get(0))
                .build()
        );
        expectedMarkedHcs.add(hc.clone().builder()
                .addVariableEdge(markingName, nodes.get(1))
                .build()
        );

        nodes.clear();
        HeapConfiguration unfoldedHc = hcFactory.getEmptyGraphWithConstants()
                .builder()
                .addNodes(type, 3, nodes)
                .addNonterminalEdge(nt)
                .addTentacle(nodes.get(0))
                .addTentacle(nodes.get(1))
                .build()
                .addVariableEdge(markingName, nodes.get(1))
                .addNonterminalEdge(nt)
                .addTentacle(nodes.get(1))
                .addTentacle(nodes.get(2))
                .build()
                .build();
        expectedMarkedHcs.add(unfoldedHc);


        ProgramState initialState = sceneObject.scene().createProgramState(hc);
        Collection<HeapConfiguration> result = generator.marked(initialState);

        assertEquals("Expected marked HCs do not coincide with computed marked HCs.",
                expectedMarkedHcs, result);
    }

}
