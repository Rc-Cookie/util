package de.rccookie.util;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Range;

/**
 * A watchdog is a timer that ensures that some process keeps responding
 * and is not in, for example, a deadlock or infinite loop. To do so, the
 * watchdog keeps waiting for a set amount of time and invokes some listeners
 * when the timer runs out. To prevent this from happening, the critical
 * process has to call the {@link #reset()} method regularly before the
 * interval times out.
 */
public class Watchdog {

    /**
     * Listeners to call when the watchdog times out.
     */
    private final List<Runnable> onTimeout = new ArrayList<>();
    /**
     * The watchdog thread.
     */
    private final WatchdogThread watchdogThread = new WatchdogThread();
    /**
     * The currently used timeout interval.
     */
    @Range(from = 1, to = Long.MAX_VALUE)
    private long interval;
    /**
     * Whether to stop the java process when the watchdog times out.
     */
    private boolean stopOnFail = false;
    /**
     * Whether the watchdog is currently suspended.
     */
    private boolean suspend = false;
    /**
     * Whether the watchdog should stop.
     */
    private boolean stop = false;
    /**
     * Monitor for synchronisation and waiting.
     */
    private final Object lock = new Object();


    /**
     * Creates a new Watchdog timer with the given timeout interval
     * and the specified listener to call when the watchdog times out.
     * The watchdog is running on a daemon thread.
     *
     * @param interval The interval after which the listener should be called
     * @param onTimeout The action to perform when the watchdog has not been
     *                  reset for the currently set interval
     */
    public Watchdog(long interval, Runnable onTimeout) {
        this(interval, onTimeout, true);
    }

    /**
     * Creates a new Watchdog timer with the given timeout interval
     * and the specified listener to call when the watchdog times out.
     *
     * @param interval The interval after which the listener should be called
     * @param onTimeout The action to perform when the watchdog has not been
     *                  reset for the currently set interval
     * @param daemon Whether the watchdog should run as daemon thread, meaning
     *               it will stop when all other non-daemon threads have
     *               terminated. Thus, it will not be able to detect program
     *               failures where the program crashes
     */
    public Watchdog(long interval, Runnable onTimeout, boolean daemon) {
        this.onTimeout.add(Arguments.checkNull(onTimeout, "onFail"));
        this.interval = Arguments.checkRange(interval, 1L, null);
        watchdogThread.setDaemon(daemon);
        watchdogThread.start(); // Start after interval is set
    }

    /**
     * Sets whether the java process should be stopped when the watchdog
     * times out. Listeners will still be called.
     *
     * @param stop Whether to stop the programm when the watchdog times out
     * @return This watchdog
     */
    public Watchdog stopOnTimeout(boolean stop) {
        this.stopOnFail = stop;
        return this;
    }

    /**
     * Adds a listener to call when the watchdog times out. If the listener gets called
     * and produces an exception, it will be caught and printed.

     * @param listener The action to perform when the watchdog has not been
     *                 reset for the currently set interval
     * @return This watchdog
     */
    public Watchdog onTimeout(Runnable listener) {
        this.onTimeout.add(Arguments.checkNull(listener, "listener"));
        return this;
    }

    /**
     * Sets the interval in which the watchdog must be reset in order for it
     * to not timeout. Note that when the interval gets changed the currently
     * running timer will be reset and started with the new interval length.
     * Thus, there may intermediately be an interval of length up to
     * {@code oldInterval + newInterval}.
     *
     * @param interval The timeout interval to set
     */
    public void setInterval(long interval) {
        if(this.interval == interval) return;
        synchronized(lock) {
            this.interval = Arguments.checkRange(interval, 1L, null);
            watchdogThread.interrupt();
        }
    }

    /**
     * Returns the interval in which the watchdog must be reset in order for it
     * to not timeout.
     *
     * @return The current timeout interval
     */
    @Range(from = 1, to = Long.MAX_VALUE)
    public long getInterval() {
        return interval;
    }

    /**
     * Resets the watchdog timer. This method must be called more frequently than
     * the set timeout interval in order for the watchdog not to timeout.
     */
    public void reset() {
        synchronized(lock) {
            watchdogThread.interrupt();
        }
    }

    /**
     * Resets the watchdog and suspends it. This means it will not run the timer
     * and will not time out if {@link #reset()} was not called ofter enough. The
     * watchdog can be resumed later on using {@link #resume()}. When the watchdog
     * is already suspended this method does nothing.
     *
     * @see #resume()
     */
    public void suspend() {
        setPaused(true);
    }

    /**
     * Resumes the watchdog, if it was suspended. Otherwise, this method does nothing.
     *
     * @see #suspend()
     */
    public void resume() {
        setPaused(false);
    }

    /**
     * Sets whether the watchdog should be suspended or running.
     *
     * @param suspend Whether the watchdog should be suspended
     * @see #suspend()
     * @see #resume()
     */
    private void setPaused(boolean suspend) {
        if(this.suspend == suspend) return;
        synchronized(lock) {
            this.suspend = suspend;
            watchdogThread.interrupt();
        }
    }

    /**
     * Permanently stops the watchdog timer. This action cannot be undone. Subsequent calls
     * to {@link #reset()}, {@link #suspend()}, {@link #resume()} and this method will have
     * no effect.
     */
    public void stop() {
        if(stop) return;
        synchronized(lock) {
            stop = true;
            watchdogThread.interrupt();
        }
    }

    /**
     * Returns whether the watchdog is still alive, meaning it has not been stopped.
     * The watchdog may still be suspended.
     *
     * @return Whether the watchdog is alive
     * @see #stop()
     */
    public boolean isAlive() {
        return watchdogThread.isAlive();
    }

    /**
     * Returns whether the watchdog is alive and not currently suspended.
     *
     * @return Whether the watchdog is currently active
     * @see #stop()
     * @see #suspend()
     * @see #isAlive()
     */
    public boolean isActive() {
        return isAlive() && !suspend;
    }

    /**
     * The watchdog timer thread.
     */
    private class WatchdogThread extends Thread {

        /**
         * Creates a new watchdog thread.
         */
        WatchdogThread() {
            super("Watchdog");
        }

        @Override
        public void run() {
            synchronized(lock) {
                while(true) {
                    try {
                        if(stop)
                            return;
                        if(suspend) {
                            lock.wait();
                            continue;
                        } else {
                            lock.wait(interval);
                        }
                        for(Runnable listener : onTimeout) {
                            try {
                                listener.run();
                            } catch(Exception e) {
                                Console.error("Exception in watchdog timeout handler:");
                                Console.error(e);
                            }
                        }
                        if(stopOnFail)
                            System.exit(0x101);
                    } catch(InterruptedException ignored) { }
                }
            }
        }
    }
}
