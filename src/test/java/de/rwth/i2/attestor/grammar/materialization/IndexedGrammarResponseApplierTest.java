package de.rwth.i2.attestor.grammar.materialization;

import de.rwth.i2.attestor.UnitTestGlobalSettings;
import de.rwth.i2.attestor.grammar.materialization.communication.CannotMaterializeException;
import de.rwth.i2.attestor.grammar.materialization.communication.GrammarResponse;
import de.rwth.i2.attestor.grammar.materialization.communication.MaterializationAndRuleResponse;
import de.rwth.i2.attestor.grammar.materialization.communication.WrongResponseTypeException;
import de.rwth.i2.attestor.grammar.materialization.indexedGrammar.IndexedGrammarResponseApplier;
import de.rwth.i2.attestor.grammar.materialization.indexedGrammar.StackMaterializer;
import de.rwth.i2.attestor.graph.GeneralSelectorLabel;
import de.rwth.i2.attestor.graph.Nonterminal;
import de.rwth.i2.attestor.graph.SelectorLabel;
import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.InternalHeapConfiguration;
import de.rwth.i2.attestor.graph.heap.internal.TestHeapConfigImplementation;
import de.rwth.i2.attestor.graph.heap.internal.TestHeapConfigurationBuilder;
import de.rwth.i2.attestor.strategies.indexedGrammarStrategies.IndexedNonterminalImpl;
import de.rwth.i2.attestor.strategies.indexedGrammarStrategies.stack.AbstractStackSymbol;
import de.rwth.i2.attestor.strategies.indexedGrammarStrategies.stack.ConcreteStackSymbol;
import de.rwth.i2.attestor.strategies.indexedGrammarStrategies.stack.StackSymbol;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.types.TypeFactory;
import de.rwth.i2.attestor.util.SingleElementUtil;
import gnu.trove.list.array.TIntArrayList;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import java.util.*;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

public class IndexedGrammarResponseApplierTest {

	private static final int EDGE_ID = -1;
	private static final String UNIQUE_NT_LABEL = "IndexedGrammarResponseApplierTest";
	private static final int RANK = 2;
	private static final boolean[] REDUCTION_TENTACLES = new boolean[]{true,false};
	private static final AbstractStackSymbol symbolToMaterialize = AbstractStackSymbol.get("X");

	@BeforeClass
	public static void init() {

		UnitTestGlobalSettings.reset();
	}


	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testDelegation() throws WrongResponseTypeException, CannotMaterializeException {
		StackMaterializer stackMaterializerMock = mock( StackMaterializer.class );
		GraphMaterializer graphMaterializerMock = mock( GraphMaterializer.class );
		
		IndexedGrammarResponseApplier ruleApplier = 
				new IndexedGrammarResponseApplier( stackMaterializerMock, graphMaterializerMock );
		
		HeapConfiguration inputGraph = createInputGraph();
		
			Map<List<StackSymbol>, Collection<HeapConfiguration> > rules = new HashMap<>();
			List<StackSymbol> materialization1 = createEmptyMaterialization();
			rules.put(materialization1, new ArrayList<>() );
		HeapConfiguration mat1_rule1 = createSimpleRule();
			rules.get(materialization1).add(mat1_rule1);
		List<StackSymbol> materialization2 = createNonEmptyMaterialization();
			rules.put(materialization2, new ArrayList<>() );
		HeapConfiguration mat2_rule1 = createSimpleRule();
			rules.get(materialization2).add(mat2_rule1);
		HeapConfiguration mat2_rule2 = createBigRule();
			rules.get(materialization2).add(mat2_rule2);
			
		AbstractStackSymbol symbolToMaterialize = AbstractStackSymbol.get("SYMBOL TO MATERIALIZE");
			
		GrammarResponse grammarResponse = new MaterializationAndRuleResponse( rules, symbolToMaterialize ); 
			
		ruleApplier.applyGrammarResponseTo(inputGraph, EDGE_ID, grammarResponse);
		verify( stackMaterializerMock ).getMaterializedCloneWith( inputGraph, symbolToMaterialize, materialization1 );
		verify( stackMaterializerMock ).getMaterializedCloneWith( inputGraph, symbolToMaterialize, materialization2 );
		verify( graphMaterializerMock, times(2) ).getMaterializedCloneWith(anyObject(), eq(EDGE_ID), eq(mat1_rule1) );
		verify( graphMaterializerMock ).getMaterializedCloneWith(anyObject(), eq(EDGE_ID), eq(mat2_rule2) );
	}

	private HeapConfiguration createInputGraph() {
		List<StackSymbol> someStack = new ArrayList<>();
		
		TestHeapConfigImplementation hc = new TestHeapConfigImplementation();
		Type type = TypeFactory.getInstance().getType("type");
		Nonterminal nt = new IndexedNonterminalImpl( UNIQUE_NT_LABEL,
												 RANK, 
												 REDUCTION_TENTACLES,
												 someStack );

		TIntArrayList nodes = new TIntArrayList();
		
		TestHeapConfigurationBuilder builder = (TestHeapConfigurationBuilder) hc.getBuilderForTest()
				.addNodes(type, 2, nodes);
		
		return	builder.addNonterminalEdge(nt, EDGE_ID, nodes ) 
				.build();
	}

	private List<StackSymbol> createEmptyMaterialization() {
		return new ArrayList<>();
	}

	private List<StackSymbol> createNonEmptyMaterialization() {
		StackSymbol bottom = ConcreteStackSymbol.getStackSymbol("Z", true);
		
		return SingleElementUtil.createList( bottom );
	}

	private HeapConfiguration createSimpleRule() {
		HeapConfiguration hc = new InternalHeapConfiguration();

		Type type = TypeFactory.getInstance().getType("type");
		SelectorLabel sel = GeneralSelectorLabel.getSelectorLabel("someSelectorLabel");

		TIntArrayList nodes = new TIntArrayList();
		return hc.builder().addNodes(type, 2, nodes)
				.setExternal( nodes.get(0) )
				.setExternal( nodes.get(1) )
				.addSelector(nodes.get(0), sel, nodes.get(1) )
				.build();
	}

	private HeapConfiguration createBigRule() {
		List<StackSymbol> someStack = new ArrayList<>();
		
		HeapConfiguration hc = new InternalHeapConfiguration();

		Type type = TypeFactory.getInstance().getType("type");
		Nonterminal nt = new IndexedNonterminalImpl( UNIQUE_NT_LABEL,
				 RANK, 
				 REDUCTION_TENTACLES,
				 someStack );
		SelectorLabel sel = GeneralSelectorLabel.getSelectorLabel("someSelectorLabel");

		TIntArrayList nodes = new TIntArrayList();
		return hc.builder().addNodes(type, 3, nodes)
				.setExternal( nodes.get(0) )
				.setExternal( nodes.get(2) )
				.addNonterminalEdge(nt)
					.addTentacle( nodes.get(0) )
					.addTentacle( nodes.get(1) )
					.build()
				.addSelector(nodes.get(1), sel, nodes.get(2) )
				.build();
	}

}
