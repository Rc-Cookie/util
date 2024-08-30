package de.rccookie.util;

import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * A general-purpose list iterator implementation. This only works efficiently
 * if the underlying list supports fast random read access (unlike linked lists).
 *
 * @param <T> The content type of the list
 */
public class RandomAccessListIterator<T> implements IterableListIterator<T> {

    protected final List<T> list;
    protected int size;
    protected int nextIndex = 0;
    protected int lastIndex = -1;

    /**
     * Creates a new list iterator for the given list.
     *
     * @param list The list to iterate
     */
    public RandomAccessListIterator(List<T> list) {
        this.list = Arguments.checkNull(list, "list");
        this.size = list.size();
    }

    /**
     * Creates a new list iterator for the given list.
     *
     * @param list The list to iterate
     * @param index The starting index. Must be >= 0 and <= list.size()
     */
    public RandomAccessListIterator(List<T> list, int index) {
        this(list);
        if(index < 0 || index > size)
            throw new IndexOutOfBoundsException(index);
    }

    @Override
    public boolean hasPrevious() {
        checkMod();
        return nextIndex > 0;
    }

    @Override
    public T previous() {
        checkMod();
        if(nextIndex <= 0) throw new EmptyIteratorException();
        T previous = list.get(nextIndex - 1);
        lastIndex = --nextIndex; // Don't modify if an exception occurs in get()
        return previous;
    }

    @Override
    public int nextIndex() {
        checkMod();
        return nextIndex;
    }

    @Override
    public int previousIndex() {
        checkMod();
        return nextIndex - 1;
    }

    @Override
    public boolean hasNext() {
        checkMod();
        return nextIndex < size;
    }

    @Override
    public T next() {
        checkMod();
        if(nextIndex >= size) throw new EmptyIteratorException();
        T next = list.get(nextIndex);
        lastIndex = nextIndex++; // Don't modify if an exception occurs in get()
        return next;
    }

    @Override
    public void remove() {
        checkMod();
        if(lastIndex == -1)
            throw new IllegalStateException("next() or previous() must be called before using remove()");
        list.remove(lastIndex);
        lastIndex = -1;
        nextIndex--;
        size--;
    }

    @Override
    public void set(T t) {
        checkMod();
        if(lastIndex == -1)
            throw new IllegalStateException("next() or previous() must be called before using set()");
        list.set(lastIndex, t);
    }

    @Override
    public void add(T t) {
        list.add(nextIndex, t);
        nextIndex++;
        size++;
        lastIndex = -1;
    }

    protected void checkMod() {
        if(list.size() != size)
            throw new ConcurrentModificationException();
    }
}
