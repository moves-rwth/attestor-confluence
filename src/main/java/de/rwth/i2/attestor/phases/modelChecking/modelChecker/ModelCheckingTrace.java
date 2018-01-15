package de.rwth.i2.attestor.phases.modelChecking.modelChecker;

import de.rwth.i2.attestor.phases.counterexamples.counterexampleGeneration.CounterexampleTrace;

import java.util.List;

public interface ModelCheckingTrace extends CounterexampleTrace {

    List<Integer> getStateIdTrace();
}
