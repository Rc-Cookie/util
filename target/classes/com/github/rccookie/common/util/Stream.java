package com.github.rccookie.common.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * A stream is a type of data structure that contains
 * a number of ordered elements that are constantly added
 * at the end and removed at the front. The next element can be
 * accessed using {@link #next()}. Once it has been
 * requested it will be irreversible removed from the stream.
 * Elements are appended using {@link #add(Object)}.
 */
public interface Stream<T> extends Iterable<T>, Iterator<T> {

    /**
     * Adds an element to the end of the stream.
     * 
     * @param element The element to add
     * @return Weather the element was added
     */
    public boolean add(T element);

    /**
     * Returns the number of elements currently contained
     * in the stream.
     * 
     * @return The current number of elements
     */
    public int size();

    /**
     * Returns the element at the given index of the stream
     * without removing it. You can use {@link #peek()} to peek
     * the first element which will therefore be returned by 
     * {@link #next()}.
     * 
     * @param index The index to peek
     * @return The element currently at that index
     * @throws NoSuchElementException If the stream does not contain
     *                                an element at that position
     */
    public T peek(int index);

    /**
     * Returns the next element of the stream without removing it.
     * 
     * @return The next element
     * @throws NoSuchElementException If the stream does not contain
     *                                any elements
     */
    public default T peek() {
        return peek(0);
    }

    @Override
    public default boolean hasNext() {
        return size() != 0;
    }

    /**
     * Returns itself. Iterating through a stream will therefore cause
     * it to be empty afterwards.
     * 
     * @return Itself
     */
    @Override
    public default Iterator<T> iterator() {
        return this;
    }
}
