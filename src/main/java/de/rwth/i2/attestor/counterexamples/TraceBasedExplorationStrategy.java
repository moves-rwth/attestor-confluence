package de.rwth.i2.attestor.counterexamples;

import de.rwth.i2.attestor.stateSpaceGeneration.ExplorationStrategy;
import de.rwth.i2.attestor.stateSpaceGeneration.ProgramState;
import de.rwth.i2.attestor.stateSpaceGeneration.StateSpace;

public class TraceBasedExplorationStrategy implements ExplorationStrategy {

    private final Trace trace;
    private final StateSubsumptionStrategy stateSubsumptionStrategy;
    private ProgramState current = null;

    public TraceBasedExplorationStrategy(Trace trace, StateSubsumptionStrategy stateSubsumptionStrategy) {

        this.trace = trace;
        this.stateSubsumptionStrategy = stateSubsumptionStrategy;
    }

    @Override
    public boolean check(ProgramState state, StateSpace stateSpace) {

        if(current == null) {
            if(trace.hasNext()) {
                current = trace.next();
            } else {
                return false;
            }
        }

        if(stateSubsumptionStrategy.subsumes(state, current)) {
            current = null; // force to move to next state next time
            return true;
        }
        return false;
    }
}
