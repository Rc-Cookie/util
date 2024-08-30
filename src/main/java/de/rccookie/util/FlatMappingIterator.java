package de.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class FlatMappingIterator<I,O> implements IterableIterator<O> {

    private final Iterator<? extends I> iterator;
    private final Function<? super I, ? extends Iterator<? extends O>> mapper;
    private Iterator<? extends O> currentIt = IterableIterator.empty();
    private O next;
    private Boolean hasNext = null;
    private Iterator<?> last = null;

    public FlatMappingIterator(Iterator<? extends I> iterator, Function<? super I, ? extends Iterator<? extends O>> mapper) {
        this.iterator = iterator;
        this.mapper = mapper;
    }

    public FlatMappingIterator(Iterable<? extends I> iterator, Function<? super I, ? extends Iterator<? extends O>> mapper) {
        this(iterator.iterator(), mapper);
    }

    @Override
    public boolean hasNext() {
        compute();
        return hasNext;
    }

    @Override
    public O next() {
        compute();
        if(!hasNext) throw new NoSuchElementException();
        hasNext = null;
        last = currentIt;
        return next;
    }

    @Override
    public void remove() {
        if(last == null)
            throw new IllegalStateException("Cannot remove because next() not called before");
        last.remove();
        last = null;
    }

    private void compute() {
        if(hasNext != null) return;
        while(!currentIt.hasNext() && iterator.hasNext())
            currentIt = mapper.apply(iterator.next());
        hasNext = currentIt.hasNext();
        if(hasNext) next = currentIt.next();
    }
}
