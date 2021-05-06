package com.github.rccookie.util;

import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * An improved of {@link java.util.Iterator Iterator} with additional functionality. Because
 * {@link Iterable} is implemented as well this iterator can be used in for-each-loops.
 *
 * @param <T> The type of element supported by this iterator
 *
 * @see java.lang.Iterable
 * @see java.util.Iterator
 * @see #peek()
 * @see #stream()
 * @see #skip()
 * @see #skip(int)
 */
public class Iterator<T> implements java.util.Iterator<T>, Iterable<T> {

    private final java.util.Iterator<T> iterator;

    private boolean isNextUpdated = false;
    private T next;

    /**
     * Creates a new Iterator from the given one.
     *
     * @param of The iterator to use. <b>Does not create a copy!</b> Using this iterator may also
     *           affect the initial one, and the other way around. Furthermore, usage of the
     *           initial iterator may lead to unexpected results.
     */
    public Iterator(java.util.Iterator<T> of) {
        iterator = of;
    }

    /**
     * Creates a new Iterator that iterates over the given iterable.
     *
     * @param overIterable The iterable to iterate over
     */
    public Iterator(Iterable<T> overIterable) {
        this(overIterable.iterator());
    }

    /**
     * Creates a new Iterator that iterates over the given stream.
     *
     * @param overStream The stream to iterate over.
     */
    public Iterator(Stream<T> overStream) {
        this(overStream.iterator());
    }

    @Override
    public java.util.Iterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        // If next is updated peek was called and didn't throw an exception and thus there must be a next item
        return isNextUpdated || iterator.hasNext();
    }

    @Override
    public T next() {
        if (!hasNext()) throw new NoSuchElementException("Iterator has no next element");
        T result = isNextUpdated ? next : iterator.next();
        isNextUpdated = false;
        return result;
    }

    /**
     * Skips the next element and returns itself. Does the same as calling the next method. However,
     * its intended use is like this:
     * <pre>Object second = iterator.skip().next();</pre>
     * Of course skipping a non existing element will throw an {@code NoSuchElementException}.
     *
     * @return This iterator
     * @throws NoSuchElementException If there is no next element
     */
    public Iterator<T> skip() {
        next();
        return this;
    }

    /**
     * Skips the next elements and returns itself. Does the same as calling the next method so many
     * times. However, its intended use is like this:
     * <pre>Object second = iterator.skip(2).next();</pre>
     * Of course skipping a non existing element will throw an {@code NoSuchElementException}.
     *
     * @param count The number of elements to skip
     * @return This iterator
     * @throws NoSuchElementException If there are no {@code count} next element
     */
    public Iterator<T> skip(int count) {
        for (int i=0; i<count; i++) {
            next();
        }
        return this;
    }

    /**
     * Returns the next element from the iteration without traversing further.
     *
     * @return The next element of the iteration
     * @throws NoSuchElementException If there is no next element
     */
    public T peek() {
        if(!hasNext()) throw new NoSuchElementException("Iterator has no next element");
        if(!isNextUpdated) {
            next = iterator.next();
            isNextUpdated = true;
        }
        return next;
    }

    /**
     * Returns a sequential steam over all remaining elements iterator. If the stream requests
     * an element, it will not be available through {@link #next()} no more.
     *
     * @return A stream over all the iterators remaining elements
     */
    public Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        forEachRemaining(action);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator, 0);
    }

    @Override
    public void remove() {
        iterator.remove();
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        iterator.forEachRemaining(action);
    }
}
