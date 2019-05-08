package de.rwth.i2.attestor.grammar.confluence.benchmark;

public class StartStopTimer {
    long timer = 0; // Accumulated time for which the timer has run
    final long TIMER_NOT_RUNNING = -1;
    long startTime = TIMER_NOT_RUNNING; // Timestamp when the timer was started

    public void startTimer() {
        if (startTime == TIMER_NOT_RUNNING) {
            startTime = System.nanoTime();
        }
    }

    public void stopTimer() {
        if (startTime != TIMER_NOT_RUNNING) {
            timer += System.nanoTime() - startTime;
            startTime = TIMER_NOT_RUNNING;
        }
    }

    public long getRuntime() {
        if (startTime == TIMER_NOT_RUNNING) {
            return timer;
        } else {
            return timer + (System.nanoTime() - startTime);
        }
    }

}
