package de.rccookie.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.rccookie.util.function.ThrowingConsumer;
import de.rccookie.util.function.ThrowingFunction;
import de.rccookie.util.function.ThrowingIntConsumer;
import de.rccookie.util.function.ThrowingIntFunction;
import de.rccookie.util.function.ThrowingIntPredicate;
import de.rccookie.util.function.ThrowingLongConsumer;
import de.rccookie.util.function.ThrowingLongFunction;
import de.rccookie.util.function.ThrowingLongPredicate;
import de.rccookie.util.function.ThrowingPredicate;
import de.rccookie.util.function.ThrowingRunnable;
import de.rccookie.util.function.ThrowingSupplier;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Utility class for parallel loops.
 */
public final class Parallel {

    private Parallel() { }


    private static final ExceptionContainer.StackTraceFilter STACK_TRACE_FILTER = new ExceptionContainer.StackTraceFilter() {
        @Override
        public int getDropCountUp(StackTraceElement[] stack) {
            int drop = 0;
            while(drop < stack.length) {
                StackTraceElement e = stack[stack.length - drop - 1];
                if(!e.getClassName().startsWith(Parallel.class.getName()) && !e.getClassName().equals(ExceptionContainer.class.getName()))
                    break;
                drop++;
            }
            return drop;
        }

        @Override
        public int getDropCountDown(StackTraceElement[] stack) {
            int drop = 0;
            while(drop < stack.length) {
                StackTraceElement e = stack[drop];
                if(!e.getClassName().startsWith(Parallel.class.getName()))
                    break;
                drop++;
            }
            return Math.max(0, drop - 1);
        }
    };


    /**
     * The parallel equivalent to
     * <pre>
     * for(int i = start; i < stop; i += increment) {
     *     body.accept(i);
     * }</pre>
     *
     * @param start The (inclusive) start of the loop
     * @param stop The (exclusive) end of the loop
     * @param increment The step size. May not be negative or 0.
     * @param body The code to execute for every number in the loop (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <E extends Throwable> void For(int start, int stop, @Range(from = 1) int increment, ThrowingIntConsumer<? extends E> body) throws E {
        For(start, stop, increment).run(body);
    }

    /**
     * The parallel equivalent to
     * <pre>
     * for(int i = start; i < stop; i++) {
     *     body.accept(i);
     * }</pre>
     *
     * @param start The (inclusive) start of the loop
     * @param stop The (exclusive) end of the loop
     * @param body The code to execute for every number between start and end (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <E extends Throwable> void For(int start, int stop, ThrowingIntConsumer<? extends E> body) throws E {
        For(start, stop).run(body);
    }

    /**
     * The parallel equivalent to
     * <pre>
     * for(int i = 0; i < count; i++) {
     *     body.accept(i);
     * }</pre>
     *
     * @param count The number of iterations for the loop, i.e., the exclusive upper bounds for the iterator number.
     * @param body The code to execute for every number between 0 and count (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <E extends Throwable> void For(int count, ThrowingIntConsumer<? extends E> body) throws E {
        For(count).run(body);
    }

    /**
     * Returns the parallel version of the for loop
     * <pre>
     * for(int i = start; i < stop; i += increment)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param start The (inclusive) start of the loop
     * @param stop The (exclusive) end of the loop
     * @param increment The step size. May not be negative or 0
     */
    public static For For(long start, long stop, long increment) {
        return new For(start, stop, increment);
    }

    /**
     * Returns the parallel version of the for loop
     * <pre>
     * for(int i = start; i < stop; i++)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param start The (inclusive) start of the loop
     * @param stop The (exclusive) end of the loop
     */
    public static For For(long start, long stop) {
        return new For(start, stop);
    }

    /**
     * Returns the parallel version of the for loop
     * <pre>
     * for(int i = 0; i < count; i++)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param count The number of iterations for the loop, i.e., the exclusive upper bounds for the iterator number.
     */
    public static For For(long count) {
        return new For(count);
    }


    /**
     * The parallel equivalent to
     * <pre>
     * for(T t : source) {
     *     body.accept(t);
     * }</pre>
     *
     * @param source The iterable to iterate. Does not require to be synchronized, calls to its iterator will be
     *               synchronized automatically.
     * @param body The code to execute for element in the source (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <T, E extends Throwable> void foreach(Iterable<? extends T> source, ThrowingConsumer<? super T, ? extends E> body) throws E {
        foreach(source).run(body);
    }

    /**
     * The parallel equivalent to
     * <pre>
     * while(source.hasNext()) {
     *     body.accept(source.next());
     * }</pre>
     *
     * @param source The iterator to iterate. Does not require to be synchronized, calls to it will be synchronized
     *               automatically.
     * @param body The code to execute for every element in the source (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <T, E extends Throwable> void foreach(Iterator<? extends T> source, ThrowingConsumer<? super T, ? extends E> body) throws E {
        foreach(source).run(body);
    }

    /**
     * The parallel equivalent to
     * <pre>
     * for(T t : source) {
     *     body.accept(t);
     * }</pre>
     *
     * @param source The iterable to iterate. Does not require to be synchronized, calls to its iterator will be
     *               synchronized automatically.
     * @param body The code to execute for element in the source (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <T, E extends Throwable> void foreach(IterableIterator<? extends T> source, ThrowingConsumer<? super T, ? extends E> body) throws E {
        foreach(source).run(body);
    }

    /**
     * The parallel equivalent to
     * <pre>
     * for(T t : source) {
     *     body.accept(t);
     * }</pre>
     *
     * @param source The array whose elements to iterate over
     * @param body The code to execute for element in the array (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <T, E extends Throwable> void foreach(T[] source, ThrowingConsumer<? super T, ? extends E> body) throws E {
        foreach(source).run(body);
    }

    /**
     * Semantically equivalent to
     * <pre>
     * source.parallel().foreach(body)</pre>
     * but allows throwing checked exceptions from within the loop body and follows Parallel.foreach syntax.
     *
     * @param source The stream whose elements to iterate over
     * @param body The code to execute for element in the stream (in an arbitrary order, on an arbitrary thread)
     * @throws E If the loop body throws an exception
     */
    public static <T, E extends Throwable> void foreach(Stream<? extends T> source, ThrowingConsumer<? super T, ? extends E> body) throws E {
        foreach(source).run(body);
    }

    /**
     * Returns the parallel version of the loop
     * <pre>
     * for(T t : source)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param source The iterable to iterate. Does not require to be synchronized, calls to its iterator will be
     *               synchronized automatically.
     */
    public static <T> Foreach<T> foreach(Iterable<? extends T> source) {
        return new Foreach<T>().source(source);
    }

    /**
     * Returns the parallel version of the loop
     * <pre>
     * while(it.hasNext()) {
     *     T t = it.next();
     *     // Here goes the loop body
     * }</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param source The iterator to iterate. Does not require to be synchronized, calls to it will be synchronized
     *               automatically.
     */
    public static <T> Foreach<T> foreach(Iterator<? extends T> source) {
        return new Foreach<T>().source(source);
    }

    /**
     * Returns the parallel version of the loop
     * <pre>
     * for(T t : source)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param source The iterable to iterate. Does not require to be synchronized, calls to its iterator will be
     *               synchronized automatically.
     */
    public static <T> Foreach<T> foreach(IterableIterator<? extends T> source) {
        return new Foreach<T>().source(source);
    }

    /**
     * Returns the parallel version of the loop
     * <pre>
     * for(T t : source)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution.
     *
     * @param source The array whose elements to iterate over
     */
    public static <T> Foreach<T> foreach(T[] source) {
        return new Foreach<T>().source(source);
    }

    /**
     * Returns the semantically equivalent loop version of
     * <pre>
     * source.parallel().forEach(body)</pre>
     * to be further customized and then executed. This method call itself does
     * not start the loop execution. Unlike with regular stream, you may set the
     * number of threads to use (lower or higher than the default of the number
     * of processor cores), and the loop body may throw checked exceptions.
     *
     * @param source The array whose elements to iterate over
     */
    public static <T> Foreach<T> foreach(Stream<? extends T> source) {
        return new Foreach<T>().source(source);
    }




    private static abstract class Loop {
        abstract int threads();
    }

    /**
     * The parallel equivalent of a for-i loop:
     * <pre>
     * for(long i = start; i < stop; i += increment) {
     *     // Do something
     * }</pre>
     * The only limitation compared to a regular for-i loop is that the increment must be positive.
     * Additionally to the bounds of the loop, this also allows to set some multithreading properties
     * and the option to break out of the loop early when a specific condition is met.
     *
     * <p>The order in which the loop is traversed is up to implementation, each iteration may be
     * performed on an arbitrary thread at an arbitrary time during the loop. The options {@link #threads(int)}
     * and {@link #balanced(boolean)} may give slight control over this, but the exact implementation
     * is not specified.</p>
     *
     * <p>The bounds of the loop can be specified as <code>long</code>. For convenience, the iteration
     * methods do however exist for both <code>int</code> and <code>long</code> (e.g. {@link #run(ThrowingIntConsumer)}
     * and {@link #runL(ThrowingLongConsumer)}). If the bounds are outside the int range but an int
     * loop body is used, the numbers will simply be cast' to ints, potentially resulting in underflows
     * or overflows. This means that the same int value may be called multiple times, but the loop itself
     * will still terminate (i.e. the internal counter variable will still use long precision).</p>
     *
     * <p>If the loop body throws any kind of exception (checked or unchecked), the loop will terminate
     * early and the exception will be rethrown on the calling thread.</p>
     *
     * <p>Instances of this class can safely be reused. Note however that changing parameters of an
     * instance which is currently executing a loop may or may not affect that loop. Neither case will
     * cause an error, but may cause unintended behavior.</p>
     */
    public static class For extends Loop {

        private long start;
        private long stop;
        private long increment;
        private int numThreads = -1;
        private boolean balanced = false;


        For(long count) {
            this(0, count);
        }

        For(long start, long stop) {
            this(start, stop, 1);
        }

        For(long start, long stop, long increment) {
            this.start = start;
            this.stop = stop;
            this.increment = Arguments.checkRange(increment, 1L, null);
        }

        /**
         * Sets the inclusive lower bound of the iterator variable. If this is higher than
         * {@link #stop(long)}, the loop body will never be executed.
         *
         * @param start The inclusive start value to set
         * @return This for loop
         */
        public For start(long start) {
            this.start = start;
            return this;
        }

        /**
         * Sets the exclusive upper bound for the iterator variable. If this is lower than
         * {@link #start(long)}, the loop body will never be executed.
         *
         * @param stop The exclusive end value to set
         * @return This for loop
         */
        public For stop(long stop) {
            this.stop = stop;
            return this;
        }

        /**
         * Sets the increment step for the loop, by which the iterator variable will be incremented
         * each loop iteration. The increment must be positive.
         *
         * @param increment The step size to set
         * @return This for loop
         */
        public For increment(@Range(from = 1) long increment) {
            this.increment = Arguments.checkRange(increment, 1L, null);
            return this;
        }

        @Override
        int threads() {
            return numThreads;
        }

        /**
         * Sets the number of threads to use to parallelize this for loop:
         * <ul>
         *     <li>A positive value greater than <code>1</code> causes the loop execution to use
         *     <i>at most</i> that many threads. The library may however choose to use less threads,
         *     particularly there are less iterations in the loop than there are threads, or if the
         *     loop already completed before all threads were started. The latter can only occur if
         *     {@link #balanced(boolean)} is set to <code>false</code>.</li>
         *     <li>A value of <code>1</code> will result in sequential execution, just like a regular
         *     for loop</li>
         *     <li>Values less or equal to <code>0</code> result in the library choosing the thread
         *     count automatically, usually 1 per CPU core. This is the default behavior.</li>
         * </ul>
         *
         * @param count The number of threads to use for execution, or <=0 to choose automatically
         * @return This for loop
         */
        public For threads(int count) {
            this.numThreads = count;
            return this;
        }

        /**
         * Controls whether the iterations should be split evenly across all threads, or whether the
         * workload time should be distributed as evenly as possible. The first may be faster if all
         * iterations naturally take the same amount of time, the latter if some iterations may take
         * longer than others, such that other threads with lighter workloads can do more iterations
         * instead. The default is <code>false</code>; the workload is split dynamically.
         *
         * @param balanced If <code>true</code>, every thread will perform the same number of iterations,
         *                 if <code>false</code> the iterations will be split dynamically based on which
         *                 thread has capacity.
         * @return This for loop
         */
        public For balanced(boolean balanced) {
            this.balanced = balanced;
            return this;
        }

        /**
         * Shorthand for <code>balanced(true)</code>.
         *
         * <p>Sets that the iterations should be split evenly across all threads, that is, each thread
         * will perform the same number of iterations of the loop (e.g. the first thread does the
         * iterations 0-99, the second thread iterations 100-199, the third 200-299 etc - the exact
         * splitting is up to the implementation). This behavior may improve performance if all
         * iterations naturally take the same amount of time (i.e. every iterations performs the same
         * action) rather than some iterations taking longer than others (which could cause some threads
         * to be waiting while others still have many iterations leftover).</p>
         *
         * @return This for loop
         */
        public For balanced() {
            return balanced(true);
        }

        /**
         * Executes this for loop, stopping early once any iteration has fulfilled the condition, that
         * is, it returned <code>true</code>. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     if(body.test(i))
         *         return true;
         * }
         * return false;</pre>
         *
         * @param body The loop body that tests whether a given number fulfills the exit condition
         * @return Whether any loop iteration returned <code>true</code>. If no iterations were performed,
         *         the result is <code>false</code>.
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> boolean anyL(ThrowingLongPredicate<? extends E> body) throws E {
            LoopExecution<E,?> execution = balanced ? new BalancedForExecution<>(this, body) : new ForExecution<>(this, body);
            return execution.execute();
        }

        /**
         * Executes this for loop, stopping early once any iteration has fulfilled the condition, that
         * is, it returned <code>true</code>. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     if(body.test((int) i))
         *         return true;
         * }
         * return false;</pre>
         *
         * @param body The loop body that tests whether a given number fulfills the exit condition
         * @return Whether any loop iteration returned <code>true</code>. If no iterations were performed,
         *         the result is <code>false</code>.
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> boolean any(ThrowingIntPredicate<? extends E> body) throws E {
            return anyL(i -> body.test((int) i));
        }

        /**
         * Executes this for loop, stopping early if any iteration has not fulfilled the condition, that
         * is, it returned <code>false</code>. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     if(!body.test(i))
         *         return false;
         * }
         * return true;</pre>
         *
         * @param body The loop body that tests whether a given number fulfills the continue condition
         * @return <code>false</code>, if any iteration returned <code>false</code>, <code>true</code>
         *         otherwise (including if the loop body was never executed)
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> boolean allL(ThrowingLongPredicate<? extends E> body) throws E {
            return !anyL(i -> !body.test(i));
        }

        /**
         * Executes this for loop, stopping early if any iteration has not fulfilled the condition, that
         * is, it returned <code>false</code>. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     if(!body.test((int) i))
         *         return false;
         * }
         * return true;</pre>
         *
         * @param body The loop body that tests whether a given number fulfills the continue condition
         * @return <code>false</code>, if any iteration returned <code>false</code>, <code>true</code>
         *         otherwise (including if the loop body was never executed)
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> boolean all(ThrowingIntPredicate<? extends E> body) throws E {
            return !anyL(i -> !body.test((int) i));
        }

        /**
         * Executes this for loop, stopping early once any iteration returned a non-null value. This is
         * equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     T t = body.apply(i);
         *     if(t != null)
         *         return t;
         * }
         * return null;</pre>
         *
         * @param body The loop body that computes the loop result value, or <code>null</code>
         * @return If any loop iteration returned a non-null value, that value is returned, otherwise
         *         <code>null</code> (including the case where the body is never executed). If multiple
         *         iterations returned a non-null value, an arbitrary one of them may be returned.
         * @throws E If thrown by the loop body
         */
        public <T, E extends Throwable> T findL(ThrowingLongFunction<? extends T, ? extends E> body) throws E {
            Arguments.checkNull(body, "body");
            Wrapper<T> result = new Wrapper<>(null);
            anyL(i -> {
                T res = body.apply(i);
                if(res != null)
                    result.value = res;
                return result.value != null;
            });
            return result.value;
        }

        /**
         * Executes this for loop, stopping early once any iteration returned a non-null value. This is
         * equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     T t = body.apply((int) i);
         *     if(t != null)
         *         return t;
         * }
         * return null;</pre>
         *
         * @param body The loop body that computes the loop result value, or <code>null</code>
         * @return If any loop iteration returned a non-null value, that value is returned, otherwise
         *         <code>null</code> (including the case where the body is never executed). If multiple
         *         iterations returned a non-null value, an arbitrary one of them may be returned.
         * @throws E If thrown by the loop body
         */
        public <T, E extends Throwable> T find(ThrowingIntFunction<? extends T, ? extends E> body) throws E {
            Arguments.checkNull(body, "body");
            Wrapper<T> result = new Wrapper<>(null);
            any(i -> {
                T res = body.apply(i);
                if(res != null)
                    result.value = res;
                return result.value != null;
            });
            return result.value;
        }

        /**
         * Executes this for loop, stopping early once any iteration returned a non-null value. If no
         * iteration returned a non-null value, the value returned by the given supplier is returned
         * instead. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     T t = body.apply(i);
         *     if(t != null)
         *         return t;
         * }
         * return elseGet.get();</pre>
         *
         * @param body The loop body that computes the loop result value, or <code>null</code>
         * @param elseGet The value to return if all iterations of the loop returned <code>null</code>.
         *                The supplier is only evaluated if all iterations returned <code>null</code>,
         *                and never more than once.
         * @return If any loop iteration returned a non-null value, that value is returned, otherwise
         *         the value returned by the given supplier (including the case where the body is
         *         never executed). If multiple iterations returned a non-null value, an arbitrary one
         *         of them may be returned.
         * @throws E If thrown by the loop body or the supplier
         */
        public <T, E extends Throwable> T findL(ThrowingLongFunction<? extends T, ? extends E> body, ThrowingSupplier<? extends T, ? extends E> elseGet) throws E {
            Arguments.checkNull(elseGet, "elseGet");
            T result = findL(body);
            return result != null ? result : elseGet.get();
        }

        /**
         * Executes this for loop, stopping early once any iteration returned a non-null value. If no
         * iteration returned a non-null value, the value returned by the given supplier is returned
         * instead. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     T t = body.apply((int) i);
         *     if(t != null)
         *         return t;
         * }
         * return elseGet.get();</pre>
         *
         * @param body The loop body that computes the loop result value, or <code>null</code>
         * @param elseGet The value to return if all iterations of the loop returned <code>null</code>.
         *                The supplier is only evaluated if all iterations returned <code>null</code>,
         *                and never more than once.
         * @return If any loop iteration returned a non-null value, that value is returned, otherwise
         *         the value returned by the given supplier (including the case where the body is
         *         never executed). If multiple iterations returned a non-null value, an arbitrary one
         *         of them may be returned.
         * @throws E If thrown by the loop body or the supplier
         */
        public <T, E extends Throwable> T find(ThrowingIntFunction<? extends T, ? extends E> body, ThrowingSupplier<? extends T, ? extends E> elseGet) throws E {
            Arguments.checkNull(elseGet, "elseGet");
            T result = find(body);
            return result != null ? result : elseGet.get();
        }

        /**
         * Executes this for loop. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     body.accept(i);
         * }</pre>
         *
         * @param body The loop body to execute for every iteration
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> void runL(ThrowingLongConsumer<? extends E> body) throws E {
            anyL(i -> {
                body.accept(i);
                return false;
            });
        }

        /**
         * Executes this for loop. This is equivalent to the sequential loop
         * <pre>
         * for(long i = start; i < stop; i += increment) {
         *     body.accept((int) i);
         * }</pre>
         *
         * @param body The loop body to execute for every iteration
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> void run(ThrowingIntConsumer<? extends E> body) throws E {
            anyL(i -> {
                body.accept((int) i);
                return false;
            });
        }
    }


    /**
     * The parallel equivalent of a foreach loop:
     * <pre>
     * for(T t : source) {
     *     // Do something
     * }</pre>
     * Additionally to source to loop over, this also allows to set some multithreading properties
     * and the option to break out of the loop early when a specific condition is met.
     *
     * <p>The order in which the source is traversed is up to implementation, each iteration may be
     * performed on an arbitrary thread at an arbitrary time during the loop. The option {@link #threads(int)}
     * may give slight control over this, but the exact implementation is not specified.</p>
     *
     * <p>If the loop body throws any kind of exception (checked or unchecked), the loop will terminate
     * early and the exception will be rethrown on the calling thread.</p>
     *
     * <p>Instances of this class can safely be reused, but this does require the source to be reusable.
     * Particularly this is not the case if the source was an {@link Iterator} or a {@link Stream}. Note
     * also that changing parameters of an instance which is currently executing a loop may or may not affect
     * that loop. Neither case will cause an error, but may cause unintended behavior.</p>
     */
    public static class Foreach<T> extends Loop {

        private Source<? extends T> source;
        private int numThreads = -1;

        /**
         * Sets the source to iterate over to the given iterator, which corresponds to the sequential loop
         * <pre>
         * while(source.hasNext()) {
         *     // Do something with source.next()
         * }</pre>
         * Note that iterators are naturally not reusable, this the foreach loop also cannot be reused without
         * changing the source.
         *
         * @param source The iterator to loop over
         * @return This foreach loop
         */
        public <U extends T> Foreach<T> source(Iterator<U> source) {
            Arguments.checkNull(source, "source");
            this.source = new Source<U>() {
                @Override
                public Stream<U> stream() {
                    return Utils.stream(source);
                }

                @NotNull
                @Override
                public Iterator<U> iterator() {
                    return source;
                }
            };
            return this;
        }

        /**
         * Sets the source to iterate over to the given iterable, which corresponds to the sequential loop
         * <pre>
         * for(T t : source) {
         *     // Do something
         * }</pre>
         *
         * @param source The iterable to loop over
         * @return This foreach loop
         */
        public <U extends T> Foreach<T> source(Iterable<U> source) {
            Arguments.checkNull(source, "source");
            this.source = new Source<U>() {
                @Override
                public Stream<U> stream() {
                    return StreamSupport.stream(source.spliterator(), true);
                }
                @NotNull
                @Override
                public Iterator<U> iterator() {
                    return source.iterator();
                }
            };
            return this;
        }

        /**
         * Sets the source to iterate over to the given iterable iterator, which corresponds to the sequential loop
         * <pre>
         * for(T t : source) {
         *     // Do something
         * }</pre>
         * Note that iterators are naturally not reusable, this the foreach loop also cannot be reused without
         * changing the source.
         *
         * @param source The iterable iterator to loop over
         * @return This foreach loop
         */
        public <U extends T> Foreach<T> source(IterableIterator<U> source) {
            return source((Iterable<U>) source);
        }

        /**
         * Sets the source to iterate over to the given array, which corresponds to the sequential loop
         * <pre>
         * for(T t : source) {
         *     // Do something
         * }</pre>
         *
         * @param source The array whose elements to loop over
         * @return This foreach loop
         */
        public Foreach<T> source(T[] source) {
            Arguments.checkNull(source, "source");
            this.source = new ArraySource<>(source);
            return this;
        }

        /**
         * Sets the source to iterate over to the given stream, which semantically corresponds to
         * <pre>
         * source.parallel().forEach(doSomething)</pre>
         * This class may be used to further control the execution parallelism, e.g. with
         * {@link #threads(int)}.
         *
         * @param source The stream whose elements to loop over
         * @return This foreach loop
         */
        public <U extends T> Foreach<T> source(Stream<U> source) {
            Arguments.checkNull(source, "source");
            this.source = new Source<U>() {
                @Override
                public Stream<U> stream() {
                    return source;
                }
                @NotNull
                @Override
                public Iterator<U> iterator() {
                    return source.iterator();
                }
            };
            return this;
        }

        @Override
        int threads() {
            return numThreads;
        }

        /**
         * Sets the number of threads to use to parallelize this for loop:
         * <ul>
         *     <li>A positive value greater than <code>1</code> causes the loop execution to use
         *     <i>at most</i> that many threads. The library may however choose to use less threads,
         *     particularly there are less iterations in the loop than there are threads, or if the
         *     loop already completed before all threads were started.</li>
         *     <li>A value of <code>1</code> will result in sequential execution, just like a regular
         *     foreach loop</li>
         *     <li>Values less or equal to <code>0</code> result in the library choosing the thread
         *     count automatically, usually 1 per CPU core. This is the default behavior.</li>
         * </ul>
         *
         * @param count The number of threads to use for execution, or <=0 to choose automatically
         * @return This foreach loop
         */
        public Foreach<T> threads(int count) {
            this.numThreads = count;
            return this;
        }

        /**
         * Executes this foreach loop, stopping early once any element has fulfilled the condition, that
         * is, the predicate returned <code>true</code>. This is equivalent to the sequential loop
         * <pre>
         * for(T t : source) {
         *     if(body.test(t))
         *         return true;
         * }
         * return false;</pre>
         *
         * @param body The loop body that tests whether a given element fulfills the exit condition
         * @return Whether any loop iteration returned <code>true</code>. If no iterations were performed,
         *         the result is <code>false</code>.
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> boolean any(ThrowingPredicate<? super T, ? extends E> body) throws E {
            if(source instanceof ArraySource) {
                T[] arr = ((ArraySource<? extends T>) source).array;
                return Parallel.For(arr.length).threads(numThreads).any(i -> body.test(arr[i]));
            }
            return new ForeachExecution<>(this, body).execute();
        }

        /**
         * Executes this foreach loop, stopping early if any element has not fulfilled the condition, that
         * is, the predicate returned <code>false</code>. This is equivalent to the sequential loop
         * <pre>
         * for(T t : source) {
         *     if(!body.test(t))
         *         return false;
         * }
         * return true;</pre>
         *
         * @param body The loop body that tests whether a given element fulfills the continue condition
         * @return <code>false</code>, if any iteration returned <code>false</code>, <code>true</code>
         *         otherwise (including if the loop body was never executed)
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> boolean all(ThrowingPredicate<? super T, ? extends E> body) throws E {
            return !any(e -> !body.test(e));
        }

        /**
         * Executes this foreach loop, stopping early once any iteration returned a non-null value. This is
         * equivalent to the sequential loop
         * <pre>
         * for(T t : source) {
         *     R r = body.apply(t);
         *     if(r != null)
         *         return r;
         * }
         * return null;</pre>
         *
         * @param body The loop body that computes the loop result value, or <code>null</code>
         * @return If any loop iteration returned a non-null value, that value is returned, otherwise
         *         <code>null</code> (including the case where the body is never executed). If multiple
         *         iterations returned a non-null value, an arbitrary one of them may be returned.
         * @throws E If thrown by the loop body
         */
        public <R, E extends Throwable> R find(ThrowingFunction<? super T, ? extends R, ? extends E> body) throws E {
            Wrapper<R> result = new Wrapper<>(null);
            any(t -> {
                R res = body.apply(t);
                if(res != null)
                    result.value = res;
                return result.value != null;
            });
            return null;
        }

        /**
         * Executes this foreach loop, stopping early once any iteration returned a non-null value. If no
         * iteration returned a non-null value, the value returned by the given supplier is returned
         * instead. This is equivalent to the sequential loop
         * <pre>
         * for(T t : source) {
         *     R r = body.apply(t);
         *     if(r != null)
         *         return r;
         * }
         * return elseGet.get();</pre>
         *
         * @param body The loop body that computes the loop result value, or <code>null</code>
         * @param elseGet The value to return if all iterations of the loop returned <code>null</code>.
         *                The supplier is only evaluated if all iterations returned <code>null</code>,
         *                and never more than once.
         * @return If any loop iteration returned a non-null value, that value is returned, otherwise
         *         the value returned by the given supplier (including the case where the body is
         *         never executed). If multiple iterations returned a non-null value, an arbitrary one
         *         of them may be returned.
         * @throws E If thrown by the loop body or the supplier
         */
        public <R, E extends Throwable> R find(ThrowingFunction<? super T, ? extends R, ? extends E> body, ThrowingSupplier<? extends R, ? extends E> elseGet) throws E {
            R result = find(body);
            return result != null ? result : elseGet.get();
        }

        /**
         * Executes this foreach loop. This is equivalent to the sequential loop
         * <pre>
         * for(T t : source) {
         *     body.accept(t);
         * }</pre>
         *
         * @param body The loop body to execute for every element
         * @throws E If thrown by the loop body
         */
        public <E extends Throwable> void run(ThrowingConsumer<? super T, ? extends E> body) throws E {
            if(source instanceof ArraySource) {
                T[] arr = ((ArraySource<? extends T>) source).array;
                Parallel.For(arr.length).threads(numThreads).any(i -> {
                    body.accept(arr[i]);
                    return false;
                });
            }
            else any(t -> {
                body.accept(t);
                return false;
            });
        }


        private interface Source<T> extends Iterable<T> {
            Stream<T> stream();
        }

        private static final class ArraySource<T> implements Source<T> {

            private final T[] array;

            private ArraySource(T[] array) {
                this.array = array;
            }

            @Override
            public Stream<T> stream() {
                return Arrays.stream(array);
            }

            @NotNull
            @Override
            public Iterator<T> iterator() {
                return Utils.iterator(array);
            }
        }
    }


    private static abstract class LoopExecution<E extends Throwable, B> {

        protected final int numThreads;
        protected final B body;

        protected ExceptionContainer<E> exceptions;
        protected boolean stop;

        protected LoopExecution(Loop loop, B body) {
            this.numThreads = loop.threads();
            this.body = Arguments.checkNull(body, "body");
        }

        protected abstract String name();

        public abstract boolean execute() throws E;


        protected void runWorkers(ThrowingRunnable<? extends E> worker) {
            int common = commonThreadCount(); // +1 for current thread
            if(numThreads <= common) {
                // Just use common pool
                LongStream.range(0, numThreads <= 0 ? common : numThreads).parallel().forEach($ -> exceptions.tryRun(worker));
            }
            else {
                IntWrapper done = new IntWrapper();

                // Spawn additional worker threads, but also use common thread pool
                for(int j = 0; j < numThreads - common; j++) {
                    // Stop spawning threads if the workload is already done
                    if(done.value == 0) {
                        new Thread(() -> {
                            exceptions.tryRun(worker);
                            synchronized(done) {
                                done.value++;
                                done.notifyAll();
                            }
                        }, "Parallel."+name()+"() worker thread "+(j+1)).start();
                    }
                    else synchronized(done) {
                        done.value += numThreads - common - j;
                        done.notifyAll();
                        break;
                    }
                }

                // Use common thread pool for remaining workers, but only if not already done
                if(done.value == 0)
                    LongStream.range(0, common).parallel().forEach($ -> exceptions.tryRun(worker));

                // Wait for all threads to finish
                synchronized(done) {
                    while(done.value < numThreads - common) try {
                        done.wait();
                    } catch(InterruptedException e) {
                        throw Utils.rethrow(e);
                    }
                }
            }
        }
    }

    private static abstract class AbstractForExecution<E extends Throwable> extends LoopExecution<E, ThrowingLongPredicate<? extends E>> {

        protected final long start;
        protected final long increment;
        protected final long count;

        public AbstractForExecution(For loop, ThrowingLongPredicate<? extends E> body) {
            super(loop, body);
            start = loop.start;
            increment = loop.increment;
            count = (loop.stop - start) / increment;
        }

        @Override
        protected String name() {
            return "For";
        }

        @Override
        public boolean execute() throws E {
            if(count <= 0)
                return false;

            if(numThreads == 1) {
                // Single-threaded -> use regular for loop
                for(long i=0; i<count; i++)
                    if(body.test(start + i * increment))
                        return true;
                return false;
            }

            exceptions = new ExceptionContainer<>(STACK_TRACE_FILTER);

            runWorkers();

            exceptions.throwIfAny();
            return stop;
        }

        protected abstract void runWorkers();
    }

    private static final class ForExecution<E extends Throwable>
            extends AbstractForExecution<E>
            implements ThrowingRunnable<E> {

        // Somehow explicit locks are way faster under heavy load (that is, many iterations
        // where the body is very short). Atomic may be faster under lower loads, but I guess
        // it doesn't really matter then anyway.

//        private final AtomicLong i = new AtomicLong();
        private long i;
        private final Lock lock = new ReentrantLock();

        public ForExecution(For loop, ThrowingLongPredicate<? extends E> body) {
            super(loop, body);
        }

        @Override
        protected void runWorkers() {
            if(numThreads <= 0 || numThreads == commonThreadCount()) {
                //noinspection ResultOfMethodCallIgnored
                LongStream.range(0, count).parallel().anyMatch(i -> stop || exceptions.hasAny() || (stop |= !exceptions.tryRun(() -> body.test(start + i * increment))));
            }
            else runWorkers(this);
        }

        @Override
        public void run() throws E {
            // From ThrowingRunnable<E>, the workload of a worker thread
            while(!stop && exceptions.hasNone()) {

//                long i = this.i.getAndIncrement();

                lock.lock();
                long i = this.i++;
                lock.unlock();

                if(i >= count || body.test(start + increment * i))
                    stop = true;
            }
        }
    }

    private static final class BalancedForExecution<E extends Throwable> extends AbstractForExecution<E> {

        private int chunkCount;

        public BalancedForExecution(For loop, ThrowingLongPredicate<? extends E> body) {
            super(loop, body);
        }

        @Override
        protected void runWorkers() {
            int common = commonThreadCount();

            chunkCount = (int) Math.min(numThreads <= 0 ? common : numThreads, count);

            if(chunkCount == common) {
                // Use stream API with anyMatch() to short-circuit if a result was found or an exception was thrown
                //noinspection ResultOfMethodCallIgnored
                LongStream.range(0, count).parallel().anyMatch(i -> stop || exceptions.hasAny() || (stop |= !exceptions.tryRun(() -> body.test(start + increment * i))));
            }
            else if(chunkCount < common) {
                // Just use common pool
                IntStream.range(0, chunkCount).parallel().forEach(chunk -> exceptions.tryRun(() -> chunkWorker(chunk)));
            }
            else {
                IntWrapper done = new IntWrapper();

                // Spawn additional worker threads, but also use common thread pool
                for(int j = 0; j < numThreads - common; j++) {
                    // Here we always need to spawn all threads, because each chunk is assigned to a specific thread only
                    int chunk = j;
                    new Thread(() -> {
                        exceptions.tryRun(() -> chunkWorker(chunk));
                        synchronized(done) {
                            done.value++;
                            done.notifyAll();
                        }
                    }, "Parallel."+name()+"() worker thread "+(j+1)).start();
                }

                // Use common thread pool for remaining workers
                IntStream.range(0, common).parallel().forEach(i -> exceptions.tryRun(() -> chunkWorker(numThreads - common + i)));

                // Wait for all threads to finish
                synchronized(done) {
                    while(done.value < numThreads - common) try {
                        done.wait();
                    } catch(InterruptedException e) {
                        throw Utils.rethrow(e);
                    }
                }
            }
        }

        private void chunkWorker(int chunk) throws E {
            long end = chunkEnd(chunk);
            for(long i = chunkStart(chunk); i<end && !stop && exceptions.hasNone(); i++) {
                if(body.test(start + increment * i)) {
                    stop = true;
                    return;
                }
            }
        }

        private long chunkStart(int chunk) {
            if(count % chunkCount == 0)
                return (count / chunkCount) * chunk;

            double size = (double) count / chunkCount;
            return (long) (size * chunk);
        }

        private long chunkEnd(int chunk) {
            return chunk == chunkCount - 1 ? count : chunkStart(chunk + 1);
        }
    }

    private static final class ForeachExecution<T, E extends Throwable>
            extends LoopExecution<E, ThrowingPredicate<? super T, ? extends E>>
            implements ThrowingRunnable<E> {

        private final Foreach.Source<? extends T> source;
        private Iterator<? extends T> it;
        private Lock itLock;


        public ForeachExecution(Foreach<T> loop, ThrowingPredicate<? super T, ? extends E> body) {
            super(loop, body);
            source = loop.source;
        }

        @Override
        protected String name() {
            return "Foreach";
        }

        @Override
        public boolean execute() throws E {

            if(numThreads == 1) {
                for(T t : source)
                    if(body.test(t))
                        return true;
                return false;
            }

            exceptions = new ExceptionContainer<>(STACK_TRACE_FILTER);

            if(numThreads <= 0 || numThreads == commonThreadCount()) {
                // Use anyMatch() to short-circuit if value is found or exception is thrown.
                // The resulting value is identical to the already set value of stop

                //noinspection ResultOfMethodCallIgnored
                source.stream().parallel().anyMatch(element -> {
                    if(exceptions.hasAny() || stop)
                        return stop = true;
                    //noinspection ConstantValue
                    return stop |= !exceptions.tryRun(() -> stop |= body.test(element));
                });
            } else {
                it = source.iterator();
                if(!it.hasNext())
                    return false;
                itLock = new ReentrantLock();

                runWorkers(this);
            }

            exceptions.throwIfAny();
            return stop;
        }

        @Override
        public void run() throws E {
            // From ThrowingRunnable<E>, represents a single loop step
            while(!stop && exceptions.hasNone()) {
                T element;

                itLock.lock();
                try {
                    if(!it.hasNext())
                        return;
                    element = it.next();
                } finally {
                    itLock.unlock();
                }

                if(stop || exceptions.hasAny() || body.test(element)) {
                    stop = true;
                    return;
                }
            }
        }
    }


    private static int commonThreadCount() {
        // Normally, these should be identical. We need +1 because the common pool has one
        // thread less than we have processors (presumably to compensate for the calling thread
        // which will also do work). However, the parallelism returned is clamped to be at
        // least 1 (for whatever reason...), thus, if we really only had a single core, we would
        // calculate with 2 common threads. Thus, clamp it to the number of processors available.
        return Math.min(
                ForkJoinPool.getCommonPoolParallelism() + 1,
                Runtime.getRuntime().availableProcessors()
        );
    }
}
