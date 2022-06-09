package com.github.rccookie.util;

import java.util.function.Function;
import java.util.stream.Stream;

public class MappedIterator<T> extends Iterator<T> {

    public <I> MappedIterator(java.util.Iterator<I> of, Function<I,T> mapper) {
        super(new java.util.Iterator<>() {
            @Override
            public boolean hasNext() {
                return of.hasNext();
            }

            @Override
            public T next() {
                return mapper.apply(of.next());
            }
        });
        Arguments.checkNull(of, "of");
        Arguments.checkNull(mapper, "mapper");
    }

    public <I> MappedIterator(Iterable<I> ofIterable, Function<I,T> mapper) {
        this(ofIterable.iterator(), mapper);
    }

    public <I> MappedIterator(Stream<I> ofStream, Function<I,T> mapper) {
        this(ofStream.iterator(), mapper);
    }
}
