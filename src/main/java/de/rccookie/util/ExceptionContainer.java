package de.rccookie.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import de.rccookie.util.function.ThrowingRunnable;
import org.jetbrains.annotations.Nullable;

/**
 * A collection of thrown exceptions. This class provides utilities to
 * collect one or more thrown exceptions from one or more threads, and
 * rethrow on a selected thread, possibly adjusting the stack traces to
 * clarify control flow across multiple threads.
 *
 * <p>All methods of this class are thread-safe. Note however that by
 * obtaining one or more exceptions, the exceptions' stack traces will
 * be adjusted to the calling thread's stack trace. Calling this multiple
 * times may modify already returned exceptions.</p>
 *
 * @param <E> The type of exceptions managed by this contained, in addition
 *            to {@link RuntimeException}s and {@link Error}s, which can
 *            always be used.
 */
public class ExceptionContainer<E extends Throwable> {

    private final StackTraceFilter stackTraceFilter;
    private Throwable main = null;
    private final List<ExceptionInfo> all = new ArrayList<>();

    /**
     * Creates a new, empty exception container which does not filter out
     * any additional stack frames when presenting the exceptions.
     */
    public ExceptionContainer() {
        this(null);
    }

    /**
     * Creates a new, empty exception container. If a stack trace filter is
     * given, it will be used to hide specific frames around the point in
     * execution, where the exceptions were thrown.
     *
     * @param stackTraceFilter A filter to specify stack frames to hide, or
     *                         <code>null</code>
     */
    public ExceptionContainer(@Nullable StackTraceFilter stackTraceFilter) {
        this.stackTraceFilter = stackTraceFilter != null ? stackTraceFilter : StackTraceFilter.SHOW_ALL;
    }

    private void addUnchecked(Throwable t, int callDepth) {
        synchronized(all) {
            if(main == null)
                main = t;
            else main.addSuppressed(t);

            StackTraceElement[] currentStack = Thread.currentThread().getStackTrace();
            StackTraceElement[] exceptionStack = t.getStackTrace();

            int sharedCount = Math.max(0, currentStack.length - 3 - callDepth);
            StackTraceElement[] localExceptionStack = Arrays.copyOfRange(exceptionStack, 0, Math.max(0, exceptionStack.length - sharedCount));

            int ignoreCount = localExceptionStack.length == 0 ? 0 : stackTraceFilter.getDropCountUp(localExceptionStack);
            all.add(new ExceptionInfo(t, Arrays.copyOfRange(localExceptionStack, 0, Math.max(0, localExceptionStack.length - ignoreCount))));
        }
    }

    /**
     * Adds the given exception to this exception container.
     *
     * @param exception The exception to add
     */
    public void add(E exception) {
        addUnchecked(Arguments.checkNull(exception, "exception"), 1);
    }

    /**
     * Adds the given exception to this exception container. Every exception
     * container can always contain unchecked exceptions (in addition to the
     * possibly checked exception type specified by the type parameter).
     *
     * @param runtimeException The exception to add
     */
    public void add(RuntimeException runtimeException) {
        addUnchecked(Arguments.checkNull(runtimeException, "runtimeException"), 1);
    }

    /**
     * Adds the given error to this exception container. Every exception
     * container can always contain unchecked exceptions (in addition to the
     * possibly checked exception type specified by the type parameter).
     *
     * @param error The error to add
     */
    public void add(Error error) {
        addUnchecked(Arguments.checkNull(error, "error"), 1);
    }

    /**
     * Adds the given exception to this exception container. Every exception
     * container can always contain unchecked exceptions (in addition to the
     * possibly checked exception type specified by the type parameter).
     *
     * <p>This method behaves identical to {@link #add(RuntimeException)}. It
     * can be useful when the generic type of this container is
     * <code>ExceptionContainer&lt;RuntimeException></code>, in which case the
     * call <code>add(someRuntimeException)</code> will be ambiguous between
     * {@link #add(RuntimeException)} and {@link #add(Throwable)}.</p>
     *
     * @param runtimeException The exception to add
     */
    public void addRuntimeException(RuntimeException runtimeException) {
        addUnchecked(Arguments.checkNull(runtimeException, "runtimeException"), 1);
    }

    /**
     * Tries to execute the given code. On success, it returns <code>true</code>.
     * If the code throws an exception, the thrown exception is caught and added
     * to the exception container, and <code>false</code> is returned. This is
     * equivalent, but more convenient to:
     * <pre>
     * try {
     *     code.run();
     *     return true;
     * } catch(RuntimeException e) {
     *     add(e);
     * } catch(Error e) {
     *     add(e);
     * } catch(Throwable t) {
     *     add((E) t);
     * }
     * </pre>
     *
     * @param code The code to execute, possibly throwing an exception
     * @return Whether the code executed successfully
     */
    public boolean tryRun(ThrowingRunnable<? extends E> code) {
        try {
            code.run();
            return true;
        } catch(Throwable t) {
            addUnchecked(t, 0);
        }
        return false;
    }

    /**
     * Returns <code>true</code> if any exceptions have been added yet to this
     * container, <code>false</code> otherwise. In other words, iff this method
     * returns <code>true</code>, a call to {@link #throwIfAny()} will throw an
     * exception.
     * <p>This is the inverse of {@link #hasNone()}.</p>
     *
     * @return Whether this container contains at least one exception
     */
    public boolean hasAny() {
        return main != null;
    }

    /**
     * Returns <code>true</code> if no exceptions have been added yet to this
     * container, <code>false</code> otherwise. In other words, iff this method
     * returns <code>true</code>, a call to {@link #throwIfAny()} will succeed
     * and not throw an exception.
     * <p>This is the inverse of {@link #hasAny()}.</p>
     *
     * @return Whether this container contains no exceptions
     */
    public boolean hasNone() {
        return main == null;
    }

    private void adjustStackTraces(int callDepth) {

        Thread currentThread = Thread.currentThread();
        StackTraceElement[] currentStack = currentThread.getStackTrace();
        currentStack = Arrays.copyOfRange(currentStack, Math.min(2 + callDepth, currentStack.length), currentStack.length);

        int dropCount = currentStack.length == 0 ? 0 : stackTraceFilter.getDropCountDown(currentStack);
        currentStack = Arrays.copyOfRange(currentStack, Math.min(dropCount, currentStack.length), currentStack.length);

        for(int i=0; i<all.size(); i++) {
            ExceptionInfo e = all.get(i);
            if(stackTraceFilter == StackTraceFilter.SHOW_ALL && e.thread == currentThread)
                e.exception.setStackTrace(e.originalStackTrace);
            else e.exception.setStackTrace(Utils.joinArray(e.localStackTrace, currentStack));
        }
    }

    /**
     * Returns a list of all exceptions that were previously added to this container,
     * in the order in which they were added. The first exception in the list (if there
     * are any exceptions present) will have the other exceptions from the list added
     * as suppressed exceptions (if that exception allows suppressed exceptions).
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * @return All exceptions in this container
     */
    public List<Throwable> getAll() {
        synchronized(all) {
            adjustStackTraces(1);
            return all.stream().map(e -> e.exception).collect(Collectors.toList());
        }
    }

    /**
     * Returns the exception first added to this container, with all subsequently
     * added exceptions added as suppressed exceptions (if the exception allows
     * suppressed exceptions), or <code>null</code> if this container is empty.
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * @return The first exception with the other exception added as suppressed,
     *         or <code>null</code> if no exceptions are present
     */
    @Nullable
    public Throwable getCombined() {
        return getCombined0();
    }

    /**
     * Like {@link #getCombined()}, but omits the calling stack frame.
     */
    @Nullable
    private Throwable getCombined0() {
        if(main == null)
            return null;

        synchronized(all) {
            adjustStackTraces(2);
            return main;
        }
    }

    /**
     * If at least one exception is present, the first added exception will be
     * thrown. Otherwise, this method does nothing and just returns regularly.
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * This method is equivalent to:
     * <pre>
     * Throwable e = getCombined();
     * if(e instanceof RuntimeException)
     *     throw (RuntimeException) e;
     * if(e instanceof Error)
     *     throw (Error) e;
     * if(e != null)
     *     throw (E) e;
     * </pre>
     *
     * @throws E The type of (possibly checked) exceptions that this container contain,
     *           and thus throw by calling this method
     */
    @SuppressWarnings("unchecked")
    public void throwIfAny() throws E {
        Throwable e = getCombined0();
        if(e == null)
            return;
        if(e instanceof RuntimeException)
            throw (RuntimeException) e;
        if(e instanceof Error)
            throw (Error) e;
        throw (E) e;
    }

    /**
     * If at least one exception is present, the first added exception will be
     * thrown, possibly wrapped in an {@link UncheckedException} if it is a checked
     * exception. Otherwise, this method does nothing and just returns regularly.
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * This method is equivalent to:
     * <pre>
     * Throwable e = getCombined();
     * if(e != null)
     *     throw Utils.rethrow(e);
     * </pre>
     */
    public void rethrowIfAny() {
        Throwable e = getCombined0();
        if(e != null)
            Utils.rethrow(e);
    }

    /**
     * Prints the exception of the first added exception and returns <code>true</code>,
     * if any exception is present, otherwise does nothing and returns <code>false</code>.
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * @return Whether a stack trace was printed, in other words, whether any exception
     *         was present
     */
    public boolean printStackTrace() {
        Throwable e = getCombined0();
        if(e == null)
            return false;
        e.printStackTrace();
        return true;
    }

    /**
     * Prints the exception of the first added exception to the specified print stream
     * and returns <code>true</code>, if any exception is present, otherwise does nothing
     * and returns <code>false</code>.
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * @param s The print stream to print the exception to, like {@link Throwable#printStackTrace(PrintStream)}
     * @return Whether a stack trace was printed, in other words, whether any exception
     *         was present
     */
    public boolean printStackTrace(PrintStream s) {
        Throwable e = getCombined0();
        if(e == null)
            return false;
        e.printStackTrace(s);
        return true;
    }

    /**
     * Prints the exception of the first added exception to the specified print writer
     * and returns <code>true</code>, if any exception is present, otherwise does nothing
     * and returns <code>false</code>.
     *
     * <p>Each exception's stack trace will be extended with the current thread's stack
     * trace, giving a more informative cross-thread stack trace.</p>
     *
     * @param s The print writer to print the exception to, like {@link Throwable#printStackTrace(PrintWriter)}
     * @return Whether a stack trace was printed, in other words, whether any exception
     *         was present
     */
    public boolean printStackTrace(PrintWriter s) {
        Throwable e = getCombined0();
        if(e == null)
            return false;
        e.printStackTrace(s);
        return true;
    }


    private static final class ExceptionInfo {
        private final Throwable exception;
        private final StackTraceElement[] originalStackTrace;
        private final StackTraceElement[] localStackTrace;
        private final Thread thread;

        private ExceptionInfo(Throwable exception, StackTraceElement[] localStackTrace) {
            this.exception = exception;
            this.originalStackTrace = exception.getStackTrace();
            this.localStackTrace = localStackTrace;
            this.thread = Thread.currentThread();
        }
    }

    /**
     * A filter specifying frames to drop from the stack trace of exceptions.
     * This can be used to hide internal execution segments from the stack trace and
     * streamline the visual stack trace, but should be used with caution; removing
     * too many stack frames may just confuse the reader and omit valuable debugging
     * information.
     */
    public interface StackTraceFilter {

        /**
         * A filter showing the full stack trace. This is the default behaviour
         * by calling {@link ExceptionContainer#ExceptionContainer()}.
         */
        StackTraceFilter SHOW_ALL = new StackTraceFilter() {
            @Override
            public int getDropCountUp(StackTraceElement[] stack) {
                return 0;
            }

            @Override
            public int getDropCountDown(StackTraceElement[] stack) {
                return 0;
            }
        };

        /**
         * Returns, for the given (partial) stack trace, the number of frames to drop,
         * starting at the bottom (last) stack frame. The topmost (first) frame represents
         * the point where the exception was thrown, the bottommost (last) frame represents
         * the statement calling the code causing the exception, which is contained in
         * the try-catch-block which caught the exception and added it to the container.
         *
         * @param stack The (partial) stack trace to filter
         * @return The number of frames to drop, starting from the bottom. 0 means keep
         *         all frames.
         */
        int getDropCountUp(StackTraceElement[] stack);

        /**
         * Returns, for the given (partial) stack trace, the number of frames to drop,
         * starting at the top (first) stack frame. The topmost (first) frame represents
         * the position in user code from which a method of the exception container was
         * called which obtains the exceptions (e.g. {@link ExceptionContainer#getAll()},
         * {@link ExceptionContainer#throwIfAny()}, etc.). The bottommost (last) frame
         * represents the bottom of the thread's stack.
         *
         * @param stack The (partial) stack trace to filter
         * @return The number of frames to drop, starting from the top. 0 means keep all
         *         frames.
         */
        int getDropCountDown(StackTraceElement[] stack);
    }
}
