package de.rccookie.util;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.function.BiConsumer;

import org.jetbrains.annotations.Nullable;

/**
 * A utility class to handle time info and fps limits in a (game) loop.
 */
public final class Time {

    /**
     * Is time measuring currently paused? => Don't count the next update call
     */
    private boolean discard = true;



    /**
     * Limit for delta time.
     */
    private float maxDelta = 0.05f;
    /**
     * Delta override, or null.
     */
    private Float fixedDelta = null;
    /**
     * Delta and time scale.
     */
    private float timeScale = 1;
    /**
     * Target duration of a frame.
     */
    private long targetFrameTime;



    /**
     * Last System.nanoTime() timestamp of the update() method exit.
     */
    private long lastNanos = System.nanoTime();

    /**
     * Duration of the last frame, in nanoseconds.
     */
    private long frameNanos = 20000000L;
    /**
     * Duration of the last frame, in seconds (unscaled).
     */
    private float delta = 0.02f;
    /**
     * Timestamp in seconds of the last update call exit, since first update call.
     */
    private float time = 0;
    /**
     * Unscaled timestamp in seconds of the last update call exit, since the first update
     * call.
     */
    private float realTime = 0;
    /**
     * Index of last update call. First update call should be indexed 0, thus -1 by default.
     */
    private long frame = -1; // first frame should be indexed 0, updated before update
    /**
     * The offset of the exact framerate from the integer framerate.
     */
    private float exactFpsOffset = 0;



    /**
     * Stores the frame durations of all frames in the last second.
     */
    private final Deque<Long> lastSecondFrameNanos = new ArrayDeque<>();
    /**
     * The sum of the frame durations in {@link #lastSecondFrameNanos}.
     */
    private long lastSecondFrameNanosSpan = 0;
    /**
     * Target timestamp of the next frame.
     */
    private long nextFrameStartTarget = System.nanoTime();
    /**
     * Scale for delays countering code execution time.
     */
    private float delayScale = 0.999925f;
    /**
     * Time in nanoseconds in which to shorten frames in order to reach the target framerate.
     */
    private long fpsRecoveryInterval = 500000000L;
    /**
     * Whether the target fps have been reached in the last frame.
     */
    private boolean targetFpsReached = false;



    /**
     * Sleep implementation to use.
     */
    private final BiConsumer<Long, Integer> sleep;



    /**
     * Creates a new time instance with the specified sleep implementation.
     *
     * @param targetFps The default target fps
     * @param sleep Sleep implementation to use
     */
    private Time(float targetFps, BiConsumer<Long, Integer> sleep) {
        this.sleep = sleep;
        this.setFpsLimit(targetFps);
    }



    /**
     * Update the time stats.
     */
    private void update() {

        // --- Test whether fps target was reached ---

        long nanos = System.nanoTime();
        targetFpsReached = targetFrameTime > 0 && (discard || nanos < nextFrameStartTarget);

        // --- Wait for target fps ---

        if(!discard && nextFrameStartTarget - nanos > 0)
            sleep.accept((nextFrameStartTarget - nanos) / 1000000, 0);

        // --- Calculate target time for next frame ---

        long frameStart = targetFrameTime == 0 ? nanos : Math.max(nextFrameStartTarget, nanos - fpsRecoveryInterval);
        nextFrameStartTarget = frameStart + (long) (targetFrameTime * delayScale);

        // --- Measure current time details ---

        frame++;
        nanos = System.nanoTime(); // Measure again after potential waiting

        if(!discard) {
            frameNanos = nanos - lastNanos;
            delta = frameNanos / 1000000000f;
            realTime += delta;
            time += delta();

            // --- Calculate fps details ---

            lastSecondFrameNanos.addLast(frameNanos);
            lastSecondFrameNanosSpan += frameNanos;

            long pastFrameTime = 0;
            while (lastSecondFrameNanosSpan > 1000000000L) {
                pastFrameTime = lastSecondFrameNanos.removeFirst();
                lastSecondFrameNanosSpan -= pastFrameTime;
            }

            exactFpsOffset = pastFrameTime != 0 ? (1000000000L - lastSecondFrameNanosSpan) / (float) pastFrameTime : 0;
        }

        // --- Reset frame discard state ---

        else discard = false;

        lastNanos = nanos;
    }

    private void discardCurrentFrame() {
        discard = true;
    }


    private void setFpsLimit(float fps) {
        targetFrameTime = !Float.isFinite(fps) || fps <= 0 ? 0 : (long) (1000000000 / (fps+0.5f));
    }

    private void setDelayScale(float scale) {
        this.delayScale = Arguments.checkInclusive(scale, 0.9f, 1f);
    }

    private void setFpsRecoveryInterval(float seconds) {
        Arguments.checkRange(seconds, 0f, null);
        this.fpsRecoveryInterval = (long) (1000000000 * seconds);
    }



    /**
     * Returns the current fps limit.
     *
     * @return The fps limit, anything higher will be delayed to achieve it
     */
    public float getFpsLimit() {
        return 1000000000f / targetFrameTime; // Infinity for targetFrameTime = 0
    }

    /**
     * Returns whether the fps limit has been reached in the last frame. If
     * there is no limit, then this method returns false.
     *
     * @return Whether the fps limit has been hit last frame
     */
    public boolean targetFpsReached() {
        return targetFpsReached;
    }

    /**
     * Returns the interval in seconds, in which frames will be shortened to reach
     * the target fps (fps limit).
     *
     * @return The current fps recovery interval
     */
    public float getFpsRecoveryInterval() {
        return fpsRecoveryInterval / 1000000000f;
    }



    /**
     * Returns the current time delta. This is the time of the last frame, in seconds. In
     * other words, summing up the delta every frame will exactly count the number of seconds
     * since start of summing up.
     *
     * @return The current time delta
     */
    public float delta() {
        return timeScale * (fixedDelta == null ? Math.min(delta, maxDelta) : fixedDelta);
    }

    /**
     * Returns the current time delta, unaffected by delta time limit and timescale.
     *
     * @return The current real time delta
     * @see #delta()
     */
    public float realDelta() {
        return delta;
    }

    /**
     * Returns the length of the last frame, in nanoseconds.
     *
     * @return The length of the last frame
     */
    public long frameNanos() {
        return frameNanos;
    }

    /**
     * Returns the time in seconds since application start.
     *
     * @return The current time in seconds
     */
    public float time() {
        return time;
    }

    /**
     * Returns the real current time since application start, unaffected by delta limit
     * and timescale.
     *
     * @return The real time in seconds
     */
    public float realTime() {
        return realTime;
    }

    /**
     * Returns the current frame index.
     *
     * @return The index of the current frame
     */
    public long frame() {
        return Math.max(0, frame); // In case it's called before the first update
    }

    /**
     * Returns the number of frames within the last second.
     *
     * @return The current framerate
     */
    public int fps() {
        return lastSecondFrameNanos.size();
    }

    /**
     * Returns the number of frames within the last second, and also accounts for
     * partial frames which weren't completely within the last second.
     *
     * @return The exact framerate
     */
    public float exactFps() {
        return fps() + exactFpsOffset;
    }

    /**
     * Returns the framerate calculated from the last frame only.
     *
     * @return The framerate based only on the last frame
     */
    public float lastFrameFps() {
        return 1000000000f / frameNanos;
    }

    /**
     * Returns the current timescale.
     *
     * @return The current timescale
     */
    public float getTimeScale() {
        return timeScale;
    }

    /**
     * Sets the timescale for delta and overall time progression. This is intended for
     * slow motion and similar.
     *
     * @param timeScale The timescale to set
     */
    public void setTimeScale(float timeScale) {
        this.timeScale = timeScale;
    }

    /**
     * Returns the maximum delta time.
     *
     * @return The current delta limit
     */
    public float getMaxDelta() {
        return maxDelta;
    }

    /**
     * Sets the delta limit per frame. This is intended to keep the deltas in a reasonable
     * range and prevent unexpected behavior on very low framerates. For example, the physics
     * engine may not be able to operate properly on very big time steps. By default, this is
     * set to {@code 0.05}, meaning that on a framerate lower than 20 fps the application will
     * run in slow motion to match that internal framerate.
     *
     * @param maxDelta The max delta to set
     */
    public void setMaxDelta(float maxDelta) {
        Arguments.checkExclusive(maxDelta, 0d, null);
        this.maxDelta = maxDelta;
    }

    /**
     * Returns the fixed delta that is currently set. {@code null} indicates that no fixed
     * time delta is set and the dynamic one is used.
     *
     * @return The currently used fixed time delta
     */
    public Float getFixedDelta() {
        return fixedDelta;
    }

    /**
     * Returns whether a fixed delta is currently used instead of a dynamically measured time
     * delta.
     *
     * @return Whether a fixed time delta is used
     */
    public boolean isFixedDelta() {
        return fixedDelta != null;
    }

    /**
     * Sets the fixed time delta to the specified value. {@code null} means to use the
     * dynamically measured time delta. It is recommended to use a dynamic time delta for
     * a smooth experience.
     *
     * @param fixedDelta The fixed time delta to set
     */
    public void setFixedDelta(@Nullable Float fixedDelta) {
        this.fixedDelta = fixedDelta;
    }



    /**
     * Handle to a {@link Time} instance which allows manipulating settings of that instance.
     */
    public final class Handle {

        /**
         * Returns the time instance that the handle controls.
         *
         * @return The handles time instance
         */
        public Time time() {
            return Time.this;
        }

        /**
         * Updates the time instance. This should be called exactly once per frame. The method
         * will block until the fps limit will be hit (if the fps would otherwise be too high),
         * if a fps limit is set.
         */
        public void update() {
            Time.this.update();
        }

        /**
         * Discards the currents frame (the one measured by the next update call) time measurement.
         * Time, delta time and fps will not be updated, and no attempt will be made to limit to the
         * fps limit. The method can be called immediately after the last update call, or immediately
         * before the update call to discard, or at any time in between, with no difference in effect.
         */
        public void discardCurrentFrame() {
            Time.this.discardCurrentFrame();
        }

        /**
         * Sets the target fps / fps limit. If the rate at which update is called will be higher than
         * the specified framerate, update will block so that that rate is achieved. If single frames
         * take longer than the average frame time, the next frames will be attempted to be shortened
         * in order to achieve the target fps. The interval in which this speed-up will be attempted
         * can be adjusted using {@link #setFpsRecoveryInterval(float)}.
         *
         * @param fps The fps limit to set. Less or equal to 0 means no limit
         */
        public void setFpsLimit(float fps) {
            Time.this.setFpsLimit(fps);
        }

        /**
         * Sets the delay factor to accommodate for code execution speed inside the update method.
         * Usually shouldn't be changed, a value between 0.9 and 1.
         *
         * @param scale The delay scale to set
         */
        public void setDelayScale(float scale) {
            Time.this.setDelayScale(scale);
        }

        /**
         * Sets the interval in seconds, in which frames will be shortened in order to hit the
         * fps limit.
         *
         * @param seconds The maximum number of seconds to try and catch up from a slow frame
         */
        public void setFpsRecoveryInterval(float seconds) {
            Time.this.setFpsRecoveryInterval(seconds);
        }
    }


    /**
     * Creates a new time instance with no fps limit.
     *
     * @return A handle to the new time instance
     */
    public static Time.Handle newInstance() {
        return newInstance(0);
    }

    /**
     * Creates a new time instance.
     *
     * @param fpsLimit The fps limit to use. Less or equal to 0 means no limit
     * @return A handle to the new time instance
     */
    public static Time.Handle newInstance(float fpsLimit) {
        return newInstance(fpsLimit, (m,n) -> {
            try {
                Thread.sleep(m,n);
            } catch(InterruptedException e) {
                Console.warn("Time update waiting interrupted:");
                Console.warn(e);
            }
        });
    }

    /**
     * Creates a new time instance.
     *
     * @param fpsLimit The fps limit to use. Less or equal to 0 means no limit
     * @param sleep The sleep implementation to use. Parameters are identical to the ones
     *              of {@link Thread#sleep(long, int)}
     * @return A handle to the new time instance
     */
    public static Time.Handle newInstance(float fpsLimit, BiConsumer<Long,Integer> sleep) {
        Time time = new Time(fpsLimit, sleep);
        return time.new Handle();
    }


    public static void main(String[] args) {
        Time.Handle time = newInstance(144);
        while(true) {
            time.update();
            Console.map("FPS", "{}, Exact: {}", time.time().fps(), time.time().exactFps());
        }
    }
}
