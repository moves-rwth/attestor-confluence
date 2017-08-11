package de.rwth.i2.attestor.semantics.jimpleSemantics.translation;

import de.rwth.i2.attestor.main.settings.Settings;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.*;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.invoke.AbstractMethod;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.invoke.InstanceInvokeHelper;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.invoke.InvokeHelper;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.invoke.StaticInvokeHelper;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.*;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.boolExpr.EqualExpr;
import de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.boolExpr.UnequalExpr;
import de.rwth.i2.attestor.types.Type;
import de.rwth.i2.attestor.types.TypeFactory;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import soot.Unit;
import soot.jimple.InstanceFieldRef;
import soot.util.Chain;

import java.util.ArrayList;
import java.util.List;

/**
 * Translator for all standard statements/values which operate on JimpleExecutables.
 * 
 * @author Hannah Arndt, Christoph
 *
 */
public class StandardAbstractSemantics implements JimpleToAbstractSemantics {

	/**
	 * The logger of this class.
	 */
	private static final Logger logger = LogManager.getLogger( "StandardAbstractSemantics" );

    /**
     * The next level in the translation hierarchy.
     */
	private final JimpleToAbstractSemantics nextLevel;

    /**
     * The topmost level in the translation hierarchy.
     */
    private TopLevelTranslation topLevel;

	/**
	 * Sets the topLevel to the given argument and nextLevel to {@link DefaultAbstractSemantics}.
     *
	 * @param topLevel The topmost level in the translation hierarchy.
	 */
	StandardAbstractSemantics(TopLevelTranslation topLevel ){
		this.topLevel = topLevel;
		this.nextLevel = new DefaultAbstractSemantics( topLevel );
	}

    /**
     * Default initialization
     */
	public StandardAbstractSemantics() {
		this.nextLevel = new DefaultAbstractSemantics( topLevel );
	}
	
	@Override
	public void setTopLevel( TopLevelTranslation topLevel ){
		this.topLevel = topLevel;
	}

	


	/**
	 * soot.jimple.AssignStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.AssignStmt AssignStmt}
	 * or {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.AssignInvoke AssignInvoke}<br>
	 * soot.jimple.IfStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.IfStmt IfStmt}<br>
	 * soot.jimple.GotoStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.GotoStmt GotoStmt}<br>
	 * soot.jimple.IdentityStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.IdentityStmt IdentityStmt}<br>
	 * soot.jimple.ReturnStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.ReturnValueStmt ReturnValueStmt}<br>
	 * soot.jimple.ReturnVoidStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.ReturnVoidStmt ReturnVoidStmt}<br>
	 * soot.jimple.InvokeStmt translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements.InvokeStmt InvokeStmt}<br>
	 * Everything else is delegated to {@link #nextLevel}
	 */
	@Override
	public Statement translateStatement( soot.jimple.Stmt input, int pc ){

		if( input instanceof soot.jimple.AssignStmt ){
					
			return translateAssignStmt( input, pc );
		}
		if( input instanceof soot.jimple.IfStmt ){
			return translateIfStmt( input, pc );
		}
		if( input instanceof soot.jimple.GotoStmt ){
			return translateGotoStmt( input );
		}
		if( input instanceof soot.jimple.IdentityStmt ){
			return translateIdentityStmt( input, pc );
		}
		if( input instanceof soot.jimple.ReturnStmt ){
			return translateReturnValueStmt( input );
		}
		if( input instanceof soot.jimple.ReturnVoidStmt ){
			return translateReturnVoidStmt();
		}
		if( input instanceof soot.jimple.InvokeStmt ){
			return translateInvokeStmt( input, pc );

		}

		logger.trace( "StandardSemantics not applicable. Using next level.." );
		return nextLevel.translateStatement( input, pc );
	}

	/**
	 * soot.jimple.NullConstant translates to {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.NullConstant NullConstant}<br>
	 * soot.Local translates to  {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.Local Local}<br>
	 * soot.InstanceFieldRef translates to  {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.Field Field} <br>
	 * soot.jimple.NewExpr translates to  {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.NewExpr NewExpr}<br>
	 * soot.jimple.IntConstant translates to  {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.IntConstant IntConstant} <br>
	 * for values in {0,1}
	 * soot.jimple.EqExpr translates to  {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.boolExpr.EqualExpr EqualExpr}<br>
	 * soot.jimple.NeExpr translates to  {@link de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.values.boolExpr.UnequalExpr UnequalExpr}<br>
	 * everything else is delegated to {@link #nextLevel}
	 */
	@Override
	public Value translateValue( soot.Value input ){

		if( input instanceof soot.jimple.NullConstant ){
			return translateNullConstant();
		}
		if( input instanceof soot.Local ){
			return translateLocal( input );
		}
		if( input instanceof InstanceFieldRef ){
			return translateField( input );
		}
		if( input instanceof soot.jimple.NewExpr ){
			return translateNewExpr( input );
		}
		if( input instanceof soot.jimple.IntConstant ){
			return translateIntConstant( input );
		}

		if( input instanceof soot.jimple.EqExpr ){
			return translateEqualExpr( input );
		}
		if( input instanceof soot.jimple.NeExpr ){
			return translateUnequalExpr( input );
		}
		logger.trace( "StandardSemantic not applicable. Using next level.." );
		return nextLevel.translateValue( input );
	}


	/**
	 * creates a NodeType with the escaped type name
	 */
	@Override
	public Type translateType( soot.Type input ){

		return TypeFactory.getInstance().getType( input.getEscapedName() );
	}
	
	/**
	 * If the left hand side of the assign is an invoke expression,
	 * the result is an AssignInvokeStmt, otherwise just an AssignStmt.
	 * @param input jimple Statement to translate
	 * @param pc current programCounter
	 * @return translated Statement
	 */
	private Statement translateAssignStmt( soot.jimple.Stmt input, int pc ) {
		logger.trace( "Recognized AssignStmt" );
		soot.jimple.AssignStmt stmt = (soot.jimple.AssignStmt) input;

		SettableValue lhs = (SettableValue) topLevel.translateValue( stmt.getLeftOp() );
		if( stmt.containsInvokeExpr() ){
			soot.jimple.InvokeExpr invokeExpr = stmt.getInvokeExpr();
			InvokeHelper invokePrepare = createInvokeHelper( invokeExpr );
			invokePrepare.setLiveVariableNames( LiveVariableHelper.extractLiveVariables( input ) );
			AbstractMethod method = topLevel.getMethod( invokeExpr.getMethod().getSignature() );
			logger.trace( "Recognized AssignInvoke" );
			return new AssignInvoke( lhs, method, invokePrepare, pc + 1 );
		}else{
			Value rhs = topLevel.translateValue( stmt.getRightOp() );
			return new AssignStmt( lhs, rhs, pc + 1, LiveVariableHelper.extractLiveVariables(input),
					Settings.getInstance().options().isRemoveDeadVariables());
		}
	}

    /**
     * Translated method invocations.
     *
     * @param input The Jimple statement representing a method invocation.
     * @param pc The program counter of the statement.
     * @return The translated invoke statement.
     */
	private InvokeStmt translateInvokeStmt( soot.jimple.Stmt input, int pc ) {
		soot.jimple.InvokeStmt stmt = (soot.jimple.InvokeStmt) input;
		soot.jimple.InvokeExpr expr = stmt.getInvokeExpr();
		// SootMethod method = expr.getMethod();

		String name = expr.getMethod().getSignature();
		AbstractMethod translatedMethod = topLevel.getMethod( name );
		// List<Type> types = method.getParameterTypes()

		InvokeHelper invokePrepare = createInvokeHelper( expr );
		invokePrepare.setLiveVariableNames( LiveVariableHelper.extractLiveVariables( input ) );
		logger.trace( "recognized InvokeStmt. " + name );
		return new InvokeStmt( translatedMethod, invokePrepare, pc + 1 );
	}
	
	/**
	 * Creates an invokeHelper. This takes into account whether it is a static or instance method invocation.
     *
	 * @param expr The invokeExpr for which the helper is created.
	 * @return The translated InvokeHelper.
	 */
	private InvokeHelper createInvokeHelper( soot.jimple.InvokeExpr expr ){
		List<soot.Value> sootParams = expr.getArgs();
		List<Value> translatedParams = new ArrayList<>();
		for( soot.Value sootParam : sootParams ){
			translatedParams.add( ( topLevel.translateValue( sootParam ) ) );
		}
		List<String> localNames = new ArrayList<>();
		if( expr.getMethod().hasActiveBody() ){
			Chain<soot.Local> locals = expr.getMethod().getActiveBody().getLocals();
			for( soot.Local local : locals ){
				localNames.add( local.getName() );
			}
		}

		InvokeHelper invokeHelper;
		if( expr instanceof soot.jimple.InstanceInvokeExpr ){
			soot.jimple.InstanceInvokeExpr instanceMethod = (soot.jimple.InstanceInvokeExpr) expr;
			soot.Value sootBase = instanceMethod.getBase();
			Value translatedBase = topLevel.translateValue( sootBase );

			invokeHelper = new InstanceInvokeHelper( translatedBase, translatedParams, localNames,
                    Settings.getInstance().options().isRemoveDeadVariables() );
		}else{
			invokeHelper = new StaticInvokeHelper( translatedParams, localNames,
                    Settings.getInstance().options().isRemoveDeadVariables() );
		}
		return invokeHelper;
	}

    /**
     * @return The translated return void statement.
     */
	private ReturnVoidStmt translateReturnVoidStmt() {
		logger.trace( "recognized Return void" );
		return new ReturnVoidStmt();
	}

    /**
     * Translate a Jimple statement to return a value.
     * @param input The Jimple statement.
     * @return The translated statement to return a value.
     */
	private ReturnValueStmt translateReturnValueStmt( soot.jimple.Stmt input ) {
		logger.trace( "recognized Return value" );
		soot.jimple.ReturnStmt stmt = (soot.jimple.ReturnStmt) input;
		Value returnValue = topLevel.translateValue( stmt.getOp() );
		Type expectedType = topLevel.translateType( stmt.getOp().getType() );
		return new ReturnValueStmt( returnValue, expectedType );
	}

    /**
     * Translates a Jimple identity statement.
     * @param input The Jimple statement.
     * @param pc The program counter of the statement.
     * @return The translated identity statement.
     */
	private IdentityStmt translateIdentityStmt( soot.jimple.Stmt input, int pc ) {
		logger.trace( "recognized Identity" );
		soot.jimple.IdentityStmt stmt = (soot.jimple.IdentityStmt) input;
		SettableValue lhs = (SettableValue) topLevel.translateValue( stmt.getLeftOp() );
		String rhs = stmt.getRightOp().toString();
		return new IdentityStmt( pc + 1, lhs, rhs );
	}

    /**
     * Translates a Jimple goto statement.
     * @param input The Jimple statement.
     * @return The translated goto statement.
     */
	private GotoStmt translateGotoStmt( soot.jimple.Stmt input ) {
		logger.trace( "recognized GotoStmt" );
		soot.jimple.GotoStmt stmt = (soot.jimple.GotoStmt) input;
		Unit successor = stmt.getTarget();
		int successorPC = topLevel.getPCforUnit( successor );
		return new GotoStmt( successorPC );
	}

    /**
     * Translates a Jimple if statement.
     * @param input The Jimple statement.
     * @param pc The program counter of the statement.
     * @return The translated if statement.
     */
	private IfStmt translateIfStmt( soot.jimple.Stmt input, int pc ) {
		logger.trace( "recognized IfStmt" );
		soot.jimple.IfStmt stmt = (soot.jimple.IfStmt) input;
		Value condition = topLevel.translateValue( stmt.getCondition() );
		Unit trueSuccessor = stmt.getTarget();
		int truePC = topLevel.getPCforUnit( trueSuccessor );
		int falsePC = pc + 1;
		return new IfStmt( condition, truePC, falsePC, LiveVariableHelper.extractLiveVariables(input),
				Settings.getInstance().options().isRemoveDeadVariables());
	}

    /**
     * Translates a Jimple unequal expression.
     * @param input A Jimple unequal expression.
     * @return The translated unequal expression.
     */
	private UnequalExpr translateUnequalExpr( soot.Value input ) {
		logger.trace( "recognized NeExpr" );
		soot.jimple.NeExpr expr = (soot.jimple.NeExpr) input;
		Value leftExpr = topLevel.translateValue( expr.getOp1() );
		Value rightExpr = topLevel.translateValue( expr.getOp2() );
		return new UnequalExpr( leftExpr, rightExpr );
	}

    /**
     * Translates a Jimple equals expression.
     * @param input A Jimple equal expression.
     * @return The translated equal expression.
     */
	private EqualExpr translateEqualExpr( soot.Value input ) {
		logger.trace( "recognized EqExpr" );
		soot.jimple.EqExpr expr = (soot.jimple.EqExpr) input;
		Value leftExpr = topLevel.translateValue( expr.getOp1() );
		Value rightExpr = topLevel.translateValue( expr.getOp2() );
		return new EqualExpr( leftExpr, rightExpr );
	}

    /**
     * Translates a Jimple new expression.
     * @param input A Jimple new expression.
     * @return The translated Jimple new expression.
     */
	private NewExpr translateNewExpr( soot.Value input ) {
		logger.trace( "recognized new" );
		soot.jimple.NewExpr expr = (soot.jimple.NewExpr) input;
		Type type = topLevel.translateType( expr.getType() );
		return new NewExpr( type );
	}

    /**
     * Translates a Jimple field expression.
     * @param input A Jimple field expression.
     * @return The translated field expression.
     */
	private Field translateField( soot.Value input ) {
		logger.trace( "recognized Field" );
		InstanceFieldRef fieldRef = (InstanceFieldRef) input;
		Value base = topLevel.translateValue( fieldRef.getBase() );
		String name = fieldRef.getField().getName();
		Type type = topLevel.translateType( fieldRef.getType() );

		return new Field( type, base, name );
	}

    /**
     * Translates a Jimple local variable.
     * @param input A Jimple local variable.
     * @return The translated local variable.
     */
	private Local translateLocal( soot.Value input ) {
		logger.trace( "recognized Local" );
		soot.Local local = (soot.Local) input;
		String name = local.getName();
		Type type = topLevel.translateType( local.getType() );

		return new Local( type, name );
	}

    /**
     * Translates a Jimple null constant.
     * @return The null constant.
     */
	private NullConstant translateNullConstant() {
		logger.trace( "recognized NullConstant" );
		return new NullConstant();
	}

    /**
     * Translates a Jimple integer constant.
     * @param input A Jimple integer constant.
     * @return The translated constant.
     */
	private Value translateIntConstant( soot.Value input ) {
		soot.jimple.IntConstant val = (soot.jimple.IntConstant) input;
		logger.trace( "Recognized IntConstant with value " + val.value );
		return new IntConstant( val.value );
	}

}
