package de.rwth.i2.attestor.grammar.canoncalization;

import de.rwth.i2.attestor.MockupSceneObject;
import de.rwth.i2.attestor.grammar.canonicalization.EmbeddingCheckerProvider;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.graph.heap.matching.AbstractMatchingChecker;
import de.rwth.i2.attestor.main.scene.SceneObject;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.ReturnVoidStmt;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.Skip;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.Statement;
import de.rwth.i2.attestor.stateSpaceGeneration.SemanticsCommand;
import de.rwth.i2.attestor.types.Type;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class DefaultEmbeddingCheckerProviderTest {

    private SceneObject sceneObject;

    public DefaultEmbeddingCheckerProviderTest() {

        this.sceneObject = new MockupSceneObject();
    }

    /**
     * aggressiveAbstractionThreshold &gt; graphSize.
     * aggressiveReturnAbstraction = true, statement != return
     * expect DepthEmbeddingChecker
     */
    @Test
    public void testSimpleCase() {

        int aggressiveAbstractionThreshold = 10;
        boolean aggressiveReturnAbstraction = true;
        HeapConfiguration graph = getGraphSmallerThan(aggressiveAbstractionThreshold);
        HeapConfiguration pattern = getPattern();
        Statement statement = new Skip(sceneObject, 0);
        boolean aggressiveNullAbstraction = sceneObject.scene().abstractionOptions().getAggressiveNullAbstraction();

        AbstractMatchingChecker expected = graph.getEmbeddingsOf(
                pattern, 1, aggressiveNullAbstraction, true
        );

        performTest(aggressiveAbstractionThreshold, aggressiveReturnAbstraction,
                graph, pattern, statement, expected);
    }

    /**
     * aggressiveAbstractionThreshold &gt; graphSize.
     * aggressiveReturnAbstraction = false, statement == return
     * expect DepthEmbeddingChecker
     */
    @Test
    public void testNormalReturn() {

        int aggressiveAbstractionThreshold = 10;
        boolean aggressiveReturnAbstraction = false;
        HeapConfiguration graph = getGraphSmallerThan(aggressiveAbstractionThreshold);
        HeapConfiguration pattern = getPattern();
        Statement statement = new ReturnVoidStmt(sceneObject);

        boolean aggressiveNullAbstraction = sceneObject.scene().abstractionOptions().getAggressiveNullAbstraction();
        AbstractMatchingChecker expected = graph.getEmbeddingsOf(
                pattern, 1, aggressiveNullAbstraction, true
        );

        performTest(aggressiveAbstractionThreshold, aggressiveReturnAbstraction,
                graph, pattern, statement, expected);
    }


    private void performTest(int aggressiveAbstractionThreshold, boolean aggressiveReturnAbstraction,
                             HeapConfiguration graph, HeapConfiguration pattern, SemanticsCommand semanticsCommand, AbstractMatchingChecker expected) {

        final int minDereferenceDepth = 1;
        boolean aggressiveNullAbstraction = sceneObject.scene().abstractionOptions().getAggressiveNullAbstraction();
        EmbeddingCheckerProvider checkerProvider =
                new EmbeddingCheckerProvider(minDereferenceDepth, aggressiveNullAbstraction, true);

        AbstractMatchingChecker checker = checkerProvider.getEmbeddingChecker(graph, pattern);

        assertEquals(expected.getClass(), checker.getClass());
        assertEquals(expected.getPattern(), checker.getPattern());
        assertEquals(expected.getTarget(), checker.getTarget());
    }

    private HeapConfiguration getPattern() {

        HeapConfiguration hc = new InternalHeapConfiguration();

        Type type = sceneObject.scene().getType("someType");

        TIntArrayList nodes = new TIntArrayList();
        return hc.builder().addNodes(type, 1, nodes).build();
    }

    private HeapConfiguration getGraphSmallerThan(int aggressiveAbstractionThreshold) {

        HeapConfiguration hc = new InternalHeapConfiguration();

        Type type = sceneObject.scene().getType("someType");

        TIntArrayList nodes = new TIntArrayList();
        return hc.builder().addNodes(type, aggressiveAbstractionThreshold - 1, nodes).build();
    }
}
