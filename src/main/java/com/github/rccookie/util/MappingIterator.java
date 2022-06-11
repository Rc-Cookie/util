package com.github.rccookie.util;

import java.util.Iterator;
import java.util.function.Function;

public class MappingIterator<I,O> implements IterableIterator<O> {

    private final Iterator<? extends I> iterator;
    private final Function<? super I, ? extends O> mapper;

    public MappingIterator(Iterator<? extends I> iterator, Function<? super I,? extends O> mapper) {
        this.iterator = Arguments.checkNull(iterator);
        this.mapper = Arguments.checkNull(mapper);
    }

    public MappingIterator(Iterable<? extends I> iterable, Function<? super I,? extends O> mapper) {
        this(iterable.iterator(), mapper);
    }

    @Override
    public boolean hasNext() {
        return iterator.hasNext();
    }

    @Override
    public O next() {
        return mapper.apply(iterator.next());
    }
}
