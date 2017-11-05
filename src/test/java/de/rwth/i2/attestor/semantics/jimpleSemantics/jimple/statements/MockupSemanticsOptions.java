package de.rwth.i2.attestor.semantics.jimpleSemantics.jimple.statements;

import de.rwth.i2.attestor.graph.heap.HeapConfiguration;
import de.rwth.i2.attestor.stateSpaceGeneration.*;
import de.rwth.i2.attestor.strategies.NoStateLabelingStrategy;
import de.rwth.i2.attestor.strategies.StateSpaceBoundedAbortStrategy;
import de.rwth.i2.attestor.strategies.defaultGrammarStrategies.DefaultProgramState;

import java.util.ArrayList;

public class MockupSemanticsOptions implements SemanticsOptions {

    @Override
    public StateSpace generateStateSpace(Program program, HeapConfiguration input, int scopeDepth) throws StateSpaceGenerationAbortedException {

        ProgramState initialState = new DefaultProgramState(input, scopeDepth);
        initialState.setProgramCounter(0);
        return StateSpaceGenerator.builder()
                .addInitialState(initialState)
                .setProgram(program)
                .setStateRefinementStrategy(s -> s)
                .setAbortStrategy(new StateSpaceBoundedAbortStrategy(500, 50))
                .setStateLabelingStrategy(new NoStateLabelingStrategy())
                .setMaterializationStrategy(
                        (state, potentialViolationPoints) -> new ArrayList<>()
                )
                .setCanonizationStrategy(
                        (semantics, conf) -> {
                            return conf.clone();
                        }
                )
                .setStateCounter( s -> {} )
                .build()
                .generate();
    }

    @Override
    public boolean isDeadVariableEliminationEnabled() {
        return false;
    }
}
