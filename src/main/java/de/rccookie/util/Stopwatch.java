package de.rccookie.util;

import java.time.Duration;
import java.util.Objects;

import de.rccookie.util.function.ThrowingRunnable;
import org.jetbrains.annotations.NotNull;

/**
 * Class to help to stop time, especially to evaluate the duration
 * of some executions.
 */
public final class Stopwatch {

    /**
     * The passed time since the last reset excluding the time passed since the last
     * start if the stopwatch is running.
     */
    private long passedTime = 0;

    /**
     * The last time the stopwatch was started.
     */
    private long lastStartTime = System.nanoTime();

    /**
     * Whether the stopwatch is currently running or not.
     */
    private boolean running = true;


    /**
     * Creates a new stopwatch. Already running.
     */
    public Stopwatch() { }


    /**
     * Start or resume the time measurement.
     *
     * @return This stopwatch
     */
    public Stopwatch start() {
        if(running) return this;
        running = true;
        lastStartTime = System.nanoTime();
        return this;
    }

    /**
     * Pause the time measurement. The time passed will still be saved and the
     * time measurement may be resumed at a later point.
     *
     * @return This stopwatch
     */
    public Stopwatch stop() {
        if(!running) return this;
        passedTime += System.nanoTime() - lastStartTime;
        running = false;
        return this;
    }

    /**
     * Reset the stopwatch. If the stopwatch is currently running it will be
     * stopped, and the elapsed time will be set to {@code 0}.
     *
     * @return This stopwatch
     */
    public Stopwatch reset() {
        running = false;
        passedTime = 0;
        return this;
    }

    /**
     * Resets the stopwatch to {@code 0} and starts time measurement.
     *
     * @return This stopwatch
     */
    public Stopwatch restart() {
        passedTime = 0;
        running = true;
        lastStartTime = System.nanoTime();
        return this;
    }


    /**
     * Determines whether this stopwatch is currently running.
     *
     * @return {@code true} if the stopwatch is currently running, {@code false}
     *         otherwise
     */
    public boolean isRunning() {
        return running;
    }

    /**
     * Returns the time passed since the last reset, in nanoseconds.
     *
     * @return The nanoseconds passed
     */
    public long getPassedNanos() {
        return passedTime + (running ? System.nanoTime() - lastStartTime : 0);
    }

    /**
     * Returns the time passed since the last reset, in microseconds.
     *
     * @return The microseconds passed
     */
    public long getPassedMicros() {
        return getPassedNanos() / 1000;
    }

    /**
     * Returns the time passed since the last reset, in microseconds, with decimal places.
     *
     * @return The microseconds passed
     */
    public double getPassedMicrosExact() {
        return getPassedNanos() / 1000d;
    }

    /**
     * Returns the time passed since the last reset, in milliseconds.
     *
     * @return The milliseconds passed
     */
    public long getPassedMillis() {
        return getPassedNanos() / 1000000;
    }

    /**
     * Returns the time passed since the last reset, in milliseconds, with decimal places.
     *
     * @return The milliseconds passed
     */
    public double getPassedMillisExact() {
        return getPassedNanos() / 1000000d;
    }

    /**
     * Returns the time passed since the last reset, in seconds.
     *
     * @return The seconds passed
     */
    public double getPassedSeconds() {
        return getPassedNanos() / 1000000000d;
    }

    /**
     * Returns the number of fully passed seconds since the last reset.
     *
     * @return The seconds fully passed
     */
    public long getFullyPassedSeconds() {
        return getPassedNanos() / 1000000000;
    }

    /**
     * Returns the time passed since the last reset, in minutes.
     *
     * @return The minutes passed
     */
    public double getPassedMinutes() {
        return getPassedSeconds() / 60;
    }

    /**
     * Returns the number of fully passed minutes since the last reset.
     *
     * @return The minutes fully passed
     */
    public int getFullyPassedMinutes() {
        return (int) (getFullyPassedSeconds() / 60);
    }

    /**
     * Returns the time passed since the last reset, in hours.
     *
     * @return The hours passed
     */
    public double getPassedHours() {
        return getPassedSeconds() / 3600;
    }

    /**
     * Returns the number of fully passed hours since the last reset.
     *
     * @return The hours fully passed
     */
    public int getFullyPassedHours() {
        return getFullyPassedMinutes() / 60;
    }

    /**
     * Returns the time passed since the last reset, in days.
     *
     * @return The days passed
     */
    public double getPassedDays() {
        return getPassedSeconds() / 86400;
    }

    /**
     * Returns the number of fully passed days since the last reset.
     *
     * @return The days fully passed
     */
    public int getFullyPassedDays() {
        return getFullyPassedMinutes() / 1440;
    }

    /**
     * Returns the time passed since the last reset, in years.
     *
     * @return The years passed
     */
    public double getPassedYears() {
        return getPassedDays() / 365.2425;
    }

    /**
     * Returns the number of fully passed years since the last reset. For this purpose,
     * one year's length is estimated to be 365 days.
     *
     * @return The years fully passed
     */
    public int getFullyPassedYearsApprox() {
        return getFullyPassedDays() / 365;
    }

    /**
     * Returns the time passed since the last reset, as a {@link Duration}.
     *
     * @return The duration passed
     */
    public Duration getPassedDuration() {
        return Duration.ofNanos(getPassedNanos());
    }

    @Override
    @NotNull
    public String toString() {
        return Utils.durationString(getPassedNanos());
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(o == null || getClass() != o.getClass()) return false;
        Stopwatch stopwatch = (Stopwatch) o;
        return passedTime == stopwatch.passedTime
            && lastStartTime == stopwatch.lastStartTime
            && running == stopwatch.running;
    }

    @Override
    public int hashCode() {
        return Objects.hash(passedTime, lastStartTime, running);
    }

    /**
     * Runs the given action with a stopwatch timing the running time.
     *
     * @param timedAction The action to be timed
     * @return The stopwatch, stopped but not yet reset. Use it to receive
     *         the duration that the execution took
     */
    @NotNull
    public static <T extends Throwable> Stopwatch timed(@NotNull ThrowingRunnable<? extends T> timedAction) throws T {
        Arguments.checkNull(timedAction, "timedAction");
        Stopwatch watch = new Stopwatch().start();
        timedAction.run();
        return watch.stop();
    }
}
