package de.rwth.i2.attestor.phases.symbolicExecution.recursive;

import de.rwth.i2.attestor.phases.symbolicExecution.procedureImpl.StateSpaceGeneratorFactory;
import de.rwth.i2.attestor.phases.symbolicExecution.recursive.interproceduralAnalysis.InterproceduralAnalysis;
import de.rwth.i2.attestor.phases.symbolicExecution.recursive.interproceduralAnalysis.PartialStateSpace;
import de.rwth.i2.attestor.phases.symbolicExecution.recursive.interproceduralAnalysis.ProcedureCall;
import de.rwth.i2.attestor.phases.symbolicExecution.recursive.interproceduralAnalysis.ProcedureRegistry;
import de.rwth.i2.attestor.procedures.Method;
import de.rwth.i2.attestor.stateSpaceGeneration.ProgramState;

public class InternalProcedureRegistry implements ProcedureRegistry {

    private final InterproceduralAnalysis analysis;
    private final StateSpaceGeneratorFactory stateSpaceGeneratorFactory;

    public InternalProcedureRegistry(InterproceduralAnalysis analysis,
                                     StateSpaceGeneratorFactory stateSpaceGeneratorFactory) {

        this.analysis = analysis;
        this.stateSpaceGeneratorFactory = stateSpaceGeneratorFactory;
    }
    
	public InternalProcedureCall getProcedureCall(Method method, ProgramState preconditionState) {
		return new InternalProcedureCall(method, preconditionState, stateSpaceGeneratorFactory);
	}

    @Override
    public void registerProcedure( ProcedureCall call ) {

        analysis.registerProcedureCall(call);
    }



    @Override
    public void registerDependency(ProgramState callingState, ProcedureCall call) {

        PartialStateSpace partialStateSpace = new InternalPartialStateSpace(callingState, stateSpaceGeneratorFactory);
        analysis.registerDependency(call, partialStateSpace);
    }

}
