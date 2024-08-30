package de.rccookie.util;

import java.util.List;
import java.util.NoSuchElementException;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Range;

/**
 * Iterator that doesn't fail if the underlying {@link List} gets
 * modified during iteration. In that case the exact objects that will
 * be iterated is not specifically defined, however, it will iterate at
 * most over as many elements as the list had at least once during the
 * iteration. Note that some elements may be iterated multiple times
 * and adding elements during the iteration may cause an infinite loop.
 */
public class ModIterator<T> implements IterableIterator<T> {

    @NotNull
    private final List<? extends T> list;
    @Range(from = 0)
    private int index = 0;
    private int lastSize;
    boolean indexUpdated = true;

    public ModIterator(@NotNull List<? extends T> list) {
        this.list = Arguments.checkNull(list);
        lastSize = list.size();
    }

    @Override
    public ModIterator<T> iterator() {
        return this;
    }

    @Override
    public boolean hasNext() {
        updateIndex();
        return index < list.size();
    }

    @Override
    public T next() {
        if(!hasNext())
            throw new NoSuchElementException();
        indexUpdated = false;
        return list.get(index++);
    }

    private void updateIndex() {
        if(!indexUpdated && lastSize > (lastSize = list.size()) && index != 0)
            index--;
        indexUpdated = true;
    }

    @Override
    public void remove() {
        updateIndex();
        if(index == 0 || index > list.size())
            throw new IllegalStateException();
        indexUpdated = false;
        list.remove(--index);
    }
}
