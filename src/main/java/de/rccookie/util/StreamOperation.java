package de.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.jetbrains.annotations.Nullable;

/**
 * A stream operation is an iterator based on another iterator.
 *
 * @param <I> The type of objects produced by the base iterator
 * @param <O> The type of objects this iterator produces
 */
public abstract class StreamOperation<I,O> implements IterableIterator<O> {

    /**
     * Access to the output of the underlying iterator.
     */
    protected final Input<I> in;
    /**
     * This should be written any produced elements to.
     */
    protected final Output<O> out = new Output<>();



    private StreamOperation(StreamOperation<?, ? extends I> in) {
        this.in = new StackedInput<>(in);
    }

    /**
     * Creates a new stream operation.
     *
     * @param in The iterator to operate on
     */
    public StreamOperation(Iterator<? extends I> in) {
        this.in = Input.forIterator(in);
    }

    /**
     * Creates a new stream operation.
     *
     * @param in The iterable on whose iterator to operate on
     */
    public StreamOperation(Iterable<? extends I> in) {
        this(Arguments.checkNull(in, "in").iterator());
    }

    /**
     * Creates a new stream operation.
     *
     * @param in The iterator to operate on
     */
    public StreamOperation(IterableIterator<? extends I> in) {
        this((Iterator<? extends I>) in);
    }

    /**
     * Creates a new stream operation.
     *
     * @param in The stream to operate on
     */
    public StreamOperation(Stream<? extends I> in) {
        this(in.iterator());
    }



    /**
     * Performs the given stream operation on the result of this stream operation.
     *
     * @param operator The operation to perform on the result of this operation
     * @return A new stream operation representing this and the given operation
     * @param <T> The result type of the given operation
     */
    public <T> StreamOperation<O,T> thenPerform(StreamOperation.Operator<? super O, ? extends T> operator) {
        Arguments.checkNull(operator, "operator");
        return new StreamOperation<>(this) {
            @SuppressWarnings({ "unchecked", "rawtypes" })
            @Override
            protected void operate(O x) {
                operator.operate(x, (Input) in, (Output) out);
            }
        };
    }



    /**
     * This function contains the actual logic of this stream operation.
     * It can read zero or more items from {@link #in}, and can to write
     * zero or more items to {@link #out}. If no items are written to
     * {@link #out} when the method is called with the last item of the
     * source iterator, this iterator is considered empty.
     *
     * <p>The most simple implementation, the identity operation, would
     * perform the operation <code>out.push(x);</code></p>.
     *
     * @param x The next item in the source iterator. Further items can be
     *          received or "looked at" using {@link #in}.
     */
    protected abstract void operate(I x);

    private void requestAtLeastOne() {
        while(!out.hasNext() && in.hasNext()) {
            in.resetPeek();
            operate(in.next());
        }
    }

    @Override
    public boolean hasNext() {
        requestAtLeastOne();
        return out.hasNext();
    }

    @Override
    public O next() {
        requestAtLeastOne();
        return out.next();
    }

    /**
     * Creates a new stream operation on the given iterator.
     *
     * @param in The iterator to operate on
     * @param operator The operation to perform
     * @return The given stream operation on the specified iterator
     */
    public static <I,O> StreamOperation<I,O> on(Iterator<? extends I> in, StreamOperation.Operator<I, O> operator) {
        Arguments.checkNull(operator, "operator");
        return new StreamOperation<>(in) {
            @Override
            protected void operate(I x) {
                operator.operate(x, in, out);
            }
        };
    }

    /**
     * Creates a new stream operation on the given iterable's iterator.
     *
     * @param in The iterable on whose iterator to operate on
     * @param operator The operation to perform
     * @return The given stream operation on the specified iterable
     */
    public static <I,O> StreamOperation<I,O> on(Iterable<? extends I> in, StreamOperation.Operator<I,O> operator) {
        return on(Arguments.checkNull(in, "in").iterator(), operator);
    }

    /**
     * Creates a new stream operation on the given iterator.
     *
     * @param in The iterator to operate on
     * @param operator The operation to perform
     * @return The given stream operation on the specified iterator
     */
    public static <I,O> StreamOperation<I,O> on(IterableIterator<? extends I> in, StreamOperation.Operator<I,O> operator) {
        return on((Iterator<? extends I>) in, operator);
    }

    /**
     * Creates a new stream operation on the given stream.
     *
     * @param in The stream to operate on
     * @param operator The operation to perform
     * @return The given stream operation on the specified stream
     */
    public static <I,O> StreamOperation<I,O> on(Stream<? extends I> in, StreamOperation.Operator<I,O> operator) {
        return on(Arguments.checkNull(in, "in").iterator(), operator);
    }



    /**
     * A utility interface that defines the custom functionality of a stream operation, that is, the
     * {@link StreamOperation#operate(I)} method, as a functional interface.
     *
     * @param <I> The type of objects produced by the base iterator
     * @param <O> The type of objects this iterator produces
     */
    @FunctionalInterface
    public interface Operator<I,O> {
        void operate(I x, Input<I> in, Output<O> out);
    }



    /**
     * A handle to the stream input, used to fetch additional items from the source iterator.
     *
     * @param <T> The type of items in the source iterator
     */
    public static abstract class Input<T> implements IterableIterator<T> {

        /**
         * The source iterator.
         */
        final Iterator<? extends T> in;
        /**
         * Items received from {@link #in} but not yet consumed by the operation.
         */
        final RingBuffer<T> buffer;
        /**
         * The index of the next item in the buffer to return from the peek method.
         */
        int peekIndex = 0;

        private Input(Iterator<? extends T> in, RingBuffer<T> buffer) {
            this.in = Arguments.checkNull(in, "in");
            this.buffer = Arguments.checkNull(buffer, "buffer");
        }

        /**
         * Returns whether the at least one more item exists, that is, calling {@link #next()}
         * will return the next item successfully.
         *
         * @return Whether more items can be consumed
         */
        @Override
        public boolean hasNext() {
            return !buffer.isEmpty() || in.hasNext();
        }

        /**
         * Returns whether <count>count</count> more items exist, that is, calling {@link #next()}
         * <code>count</code> times will return all those next items successfully.
         *
         * @param count The number of items to check ahead
         * @return Whether so many items are available
         */
        public abstract boolean hasNext(int count);

        /**
         * Returns and consumes the next item.
         *
         * @return The next item
         * @throws NoSuchElementException If no more items are available
         */
        @Override
        public T next() {
            if(!buffer.isEmpty()) return buffer.remove();
            peekIndex = Math.max(peekIndex - 1, 0);
            return in.next();
        }

        /**
         * Returns and consumes the next item if more items are available, otherwise returns null.
         *
         * @return The next item or null
         */
        public T tryNext() {
            if(!buffer.isEmpty()) return buffer.remove();
            if(!in.hasNext()) return null;
            peekIndex = Math.max(peekIndex - 1, 0);
            return in.next();
        }

        /**
         * Returns and consumes the next item, if it exists and matches the given requirement.
         * Otherwise, the item is not consumed and <code>null</code> is returned.
         *
         * <p>
         * This method behaves exactly as the following:
         * <pre>
         * return hasNext() && requirement.test(peek(0)) ? next() : null;
         * </pre>
         * </p>
         *
         * @param requirement The requirement for an item to meet
         * @return The next item if it met the requirement, or null
         */
        @Nullable
        public T nextIf(Predicate<T> requirement) {
            return hasNext() && requirement.test(peek(0)) ? next() : null;
        }

        /**
         * Returns, but does not consume the next item that would be returned by {@link #next()}, if
         * any, otherwise <code>null</code>. When called multiple times, the subsequent items will be
         * returned! {@link #resetPeek()} can be used to reset the peeking position to the actually
         * next item, which is done automatically before each {@link StreamOperation#operate(T)} call.
         *
         * @return The next item, or null
         */
        public abstract T peek();

        /**
         * Returns the item that would be returned by {@link #next()} after skipping <code>offset</code>
         * many items, if it exists, otherwise <code>null</code>. <b>This method is independent of the
         * peek offset from normal peek() calls!</b>
         *
         * @param offset The number of items to skip, always from the item last returned by {@link #next()}
         * @return The item at that offset, or null
         */
        public abstract T peek(int offset);

        /**
         * Returns whether more item(s) are available, at the current peek offset.
         *
         * @return Whether a {@link #peek()} call would successfully return the next item at the peek offset
         */
        public boolean hasNextPeek() {
            return buffer.size() > peekIndex || in.hasNext();
        }

        /**
         * Resets the peek offset so that a subsequent call to {@link #peek()} actually
         * returns the item that would be returned by {@link #next()}. This is done
         * automatically before every {@link StreamOperation#operate(T)} call.
         */
        public void resetPeek() {
            peekIndex = 0;
        }

        /**
         * Consumes and discards the next item. Equivalent to calling {@link #next()} and discarding the result.
         *
         * @throws NoSuchElementException If no next element to skip is available
         */
        public void skip() {
            if(buffer.isEmpty()) in.next();
            else {
                buffer.remove();
                peekIndex = Math.max(peekIndex - 1, 0);
            }
        }

        /**
         * Consumes and discards the next item, if there is any. Equivalent to calling {@link #tryNext()} and
         * discarding the result.
         */
        public void trySkip() {
            if(!buffer.isEmpty()) {
                buffer.remove();
                peekIndex = Math.max(peekIndex - 1, 0);
            }
            else if(in.hasNext()) in.next();
        }

        /**
         * Consumes and discards the next <code>count</code> items. Equivalent to calling {@link #next()}
         * <code>count</code> times and discarding the results.
         *
         * @param count The number of items to skip
         * @throws NoSuchElementException If less than <code>count</code> next elements are available
         */
        public void skip(int count) {
            int i = buffer.remove(count);
            peekIndex = Math.max(peekIndex - i, 0);
            for(; i<count; i++)
                in.next();
        }

        /**
         * Consumes and discards the next <code>maxCount</code> items, or all if less are available. Equivalent
         * to calling {@link #tryNext()} <code>maxCount</code> times and discarding the results.
         *
         * @param maxCount The number of items to skip if available
         * @return The number of items actually skipped
         */
        public int trySkip(int maxCount) {
            int i = buffer.remove(maxCount);
            peekIndex = Math.max(peekIndex - i, 0);
            for(; i<maxCount; i++) {
                if(!in.hasNext()) return i;
                in.next();
            }
            return maxCount;
        }

        /**
         * Consumes and discards the next item, if it exists and meets the given
         * requirement. Otherwise, the item is not consumed.
         *
         * <p>
         * This method behaves exactly as the following:
         * <pre>
         * if(!hasNext() || !requirement.test(peek(0))) return false;
         * skip();
         * return true;
         * </pre>
         * </p>
         *
         * @param requirement The requirement the item has to meet to be skipped
         * @return Whether an item was skipped
         */
        public boolean skipIf(Predicate<T> requirement) {
            if(!hasNext() || !requirement.test(peek(0))) return false;
            skip();
            return true;
        }

        /**
         * Consumes and discards subsequent items while they exist and match the given
         * requirement.
         *
         * @param requirement The requirement for items to be skipped
         * @return How many items were skipped
         */
        public int skipWhile(Predicate<T> requirement) {
            int count = 0;
            for(; hasNext() && requirement.test(peek(0)); count++)
                skip();
            return count;
        }

        /**
         * Returns an input object for the given source iterator.
         *
         * @param iterator The iterator to use as source for the input
         * @return An input over the given iterator
         */
        @SuppressWarnings("unchecked")
        private static <T> Input<T> forIterator(Iterator<? extends T> iterator) {
            return iterator instanceof StreamOperation ? new StackedInput<>((StreamOperation<?, ? extends T>) iterator) : new SimpleInput<>(iterator);
        }
    }

    /**
     * Default implementation of {@link Input} using a dedicated buffer as input buffer.
     */
    private static final class SimpleInput<T> extends Input<T> {

        private SimpleInput(Iterator<? extends T> in) {
            super(in, new RingBuffer<>());
        }

        @Override
        public boolean hasNext(int count) {
            Arguments.checkRange(count, 0, null);
            for(int i=buffer.size(); i<count; i++) {
                if(!in.hasNext()) return false;
                buffer.add(in.next());
            }
            return true;
        }

        @Override
        public T peek() {
            if(buffer.size() > peekIndex)
                return buffer.get(peekIndex++);
            if(in.hasNext()) {
                peekIndex++;
                return buffer.add(in.next());
            }
            return null;
        }

        @Override
        public T peek(int offset) {
            while(buffer.size() <= offset) {
                if(!in.hasNext()) return null;
                buffer.add(in.next());
            }
            return buffer.get(offset);
        }
    }

    /**
     * Optimized implementation of {@link Input} using the previous' stream operation's
     * {@link Output}'s buffer directly as input buffer.
     */
    private static final class StackedInput<T> extends Input<T> {

        private final StreamOperation<?, ? extends T> inOp;

        @SuppressWarnings("unchecked")
        private StackedInput(StreamOperation<?, ? extends T> in) {
            super(in, (RingBuffer<T>) in.out.buffer); // This is ok because we only do read access, so it's fine if it only allows a subclass type
            inOp = in;
        }

        @Override
        public boolean hasNext(int count) {
            Arguments.checkRange(count, 0, null);
            for(int i=buffer.size(); i<count; i++)
                if(!in.hasNext()) return false; // Will call requestAtLeastOne(), writing the result into the shared buffer
            return true;
        }

        @Override
        public T peek() {
            if(buffer.size() > peekIndex)
                return buffer.get(peekIndex++);
            if(in.hasNext()) { // Will call requestAtLeastOne(), writing the result into the shared buffer
                peekIndex++;
                return buffer.get();
            }
            return null;
        }

        @Override
        public T peek(int offset) {
            while(buffer.size() <= offset)
                if(!in.hasNext()) return null; // Will call requestAtLeastOne(), writing the result into the shared buffer
            return buffer.get(offset);
        }
    }


    /**
     * A handle to write items to be produced to. This replaces the normal return value
     * used by a regular iterator and allows to return zero, one or more items for a single
     * input item.
     */
    public static final class Output<T> implements Iterator<T> {

        /**
         * Items written, but not yet read from the stream operation iterator.
         */
        private final RingBuffer<T> buffer = new RingBuffer<>();

        private Output() { }

        /**
         * Returns the given value from the stream operation. When called multiple times
         * the values will be returned in the order of the method calls.
         *
         * @param value The value to return from the stream operation
         */
        public void push(T value) {
            buffer.add(value);
        }

        /**
         * Returns the given values from the stream operation, in the same order as in
         * the array. Equivalent to calling {@link #push(T)} once for each item.
         *
         * @param values The values to be returned from the stream operation
         */
        @SafeVarargs
        public final void push(T... values) {
            buffer.add(values);
        }

        /**
         * Internal.
         */
        @Override
        public boolean hasNext() {
            return !buffer.isEmpty();
        }

        /**
         * Internal.
         */
        @Override
        public T next() {
            if(buffer.isEmpty()) throw new EmptyIteratorException();
            return buffer.remove();
        }
    }
}
