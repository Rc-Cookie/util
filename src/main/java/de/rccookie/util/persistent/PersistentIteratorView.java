package de.rccookie.util.persistent;

import java.util.Iterator;
import java.util.function.Consumer;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public class PersistentIteratorView<T> extends AbstractPersistentView<Iterator<T>> implements Iterator<T> {

    public <U> PersistentIteratorView(PersistentData<? extends U> backingData, Function<? super U, ? extends Iterator<T>> iterator) {
        super(backingData, Arguments.checkNull(iterator, "iterator"));
    }

    public PersistentIteratorView(PersistentData<?> backingData, Iterator<T> iterator) {
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
}
