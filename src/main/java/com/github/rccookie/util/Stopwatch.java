package com.github.rccookie.util;

import java.util.Objects;

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
    private long lastStartTime = 0;

    /**
     * Whether the stopwatch is currently running or not.
     */
    private boolean running = false;


    /**
     * Creates a new stopwatch. Must be started with {@link #start()} to begin
     * measuring time.
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
        stop();
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
        start();
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
     * Returns the time passed since the last reset, in milliseconds.
     *
     * @return The milliseconds passed
     */
    public long getPassedMillis() {
        return getPassedNanos() / 1000000;
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
     * Returns the time passed since the last reset, in minutes.
     *
     * @return The minutes passed
     */
    public double getPassedMinutes() {
        return getPassedSeconds() / 60;
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
     * Returns the time passed since the last reset, in days.
     *
     * @return The days passed
     */
    public double getPassedDays() {
        return getPassedSeconds() / 86400;
    }

    /**
     * Returns the time passed since the last reset, in years.
     *
     * @return The years passed
     */
    public double getPassedYears() {
        return getPassedDays() / 31556736;
    }

    @Override
    @NotNull
    public String toString() {
        StringBuilder string = new StringBuilder();
        double years   = getPassedYears();
        double days    = getPassedDays();
        double hours   = getPassedHours();
        double minutes = getPassedMinutes();
        double seconds = getPassedSeconds();
        long nanos     = getPassedNanos();
        boolean anythingWritten = false;

        if(years >= 1) {
            years = (int) years;
            string.append(String.format("%02d", (int) years)).append("y:");
            days -= 365.24 * years;
            hours -= 8765.76 * years;
            minutes -= 525945.6 * years;
            seconds -= 31556736 * years;
            nanos -= 3.1556736E16 * years;
            anythingWritten = true;
        }
        if(days >= 1) {
            days = (int) days;
            string.append(String.format("%02d", (int) days)).append("d:");
            hours -= 24 * days;
            minutes -= 1440 * days;
            seconds -= 86400 * days;
            nanos -= 86400000000000L * days;
            anythingWritten = true;
        }
        else if(anythingWritten) string.append("00:");
        if(hours >= 1) {
            hours = (int) hours;
            string.append(String.format("%02d", (int) hours)).append("h:");
            minutes -= 60 * hours;
            seconds -= 3600 * hours;
            nanos -= 3600000000000L * hours;
            anythingWritten = true;
        }
        else if(anythingWritten) string.append("00:");
        if(minutes >= 1) {
            minutes = (int) minutes;
            string.append(String.format("%02d", (int) minutes)).append("m:");
            seconds -= 60 * minutes;
            nanos -= 60000000000L * minutes;
            anythingWritten = true;
        }
        else if(anythingWritten) string.append("00:");
        if(seconds >= 1) {
            seconds = (int) seconds;
            string.append(String.format("%02d", (int) seconds)).append("s:");
            nanos -= 1000000000L * seconds;
            anythingWritten = true;
        }
        else if(anythingWritten) string.append("00:");

        String nanoString = String.format("%09d", nanos);
        while(nanoString.endsWith("0"))
            nanoString = nanoString.substring(0, nanoString.length() - 1);
        string.append(nanoString);
        if(anythingWritten) string.append("n");
        if(running) string.append(" (running)");

        return string.toString();
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
}
