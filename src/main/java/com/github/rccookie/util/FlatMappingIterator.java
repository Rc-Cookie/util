package com.github.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Function;

public class FlatMappingIterator<I,O> implements IterableIterator<O> {

    private final Iterator<? extends I> iterator;
    private final Function<? super I, ? extends Iterator<? extends O>> mapper;
    private Iterator<? extends O> currentIt = null;
    private O next;
    private Boolean hasNext = null;

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
        return next;
    }

    private void compute() {
        if(hasNext != null) return;
        while(!currentIt.hasNext() && iterator.hasNext())
            currentIt = mapper.apply(iterator.next());
        hasNext = currentIt.hasNext();
        if(hasNext) next = currentIt.next();
    }
}
