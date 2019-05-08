package de.rwth.i2.attestor.grammar.confluence.benchmark;

/**
 * Allows to measure time. If the timer is started multiple times, the timer has to be stopped the same number of times.
 */
public class StartStopTimer {
    long timer = 0; // Accumulated time for which the timer has run
    final long TIMER_NOT_RUNNING = -1;
    long startTime = TIMER_NOT_RUNNING; // Timestamp when the timer was started
    int timerDepth = 0;

    public void startTimer() {
        if (startTime == TIMER_NOT_RUNNING) {
            startTime = System.nanoTime();
        }
        timerDepth++;
    }

    public void stopTimer() {
        timerDepth--;
        if (timerDepth < 0 || startTime == TIMER_NOT_RUNNING) {
            throw new RuntimeException("The timer is not running. Cannot stop a non running timer.");
        } else if (timerDepth == 0) {
            timer += System.nanoTime() - startTime;
            startTime = TIMER_NOT_RUNNING;
        } // else: Did not reach timerDepth == 0 -> Keep timer running
    }

    public long getRuntime() {
        if (startTime == TIMER_NOT_RUNNING) {
            return timer;
        } else {
            return timer + (System.nanoTime() - startTime);
        }
    }

}
