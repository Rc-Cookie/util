package de.rccookie.util.persistent;

import java.util.Iterator;
import java.util.ListIterator;
import java.util.function.Consumer;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public class PersistentListIteratorView<T> extends AbstractPersistentView<ListIterator<T>> implements ListIterator<T> {

    public <U> PersistentListIteratorView(PersistentData<? extends U> backingData, Function<? super U, ? extends ListIterator<T>> iterator) {
        super(backingData, Arguments.checkNull(iterator, "iterator"));
    }

    public PersistentListIteratorView(PersistentData<?> backingData, ListIterator<T> iterator) {
        super(backingData, Arguments.checkNull(iterator, "iterator"));
    }

    @Override
    public boolean hasNext() {
        return readLocked(Iterator::hasNext);
    }

    @Override
    public T next() {
        return readLocked(Iterator::next);
    }

    @Override
    public void remove() {
        doWriteLocked(Iterator::remove);
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        doReadLocked(it -> it.forEachRemaining(action));
    }

    @Override
    public boolean hasPrevious() {
        return readLocked(ListIterator::hasPrevious);
    }

    @Override
    public T previous() {
        return readLocked(ListIterator::previous);
    }

    @Override
    public int nextIndex() {
        return readLocked(ListIterator::nextIndex);
    }

    @Override
    public int previousIndex() {
        return readLocked(ListIterator::previousIndex);
    }

    @Override
    public void set(T t) {
        doWriteLocked(it -> it.set(t));
    }

    @Override
    public void add(T t) {
        doWriteLocked(it -> it.add(t));
    }
}
