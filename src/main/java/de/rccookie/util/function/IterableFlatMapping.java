package de.rccookie.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface IterableFlatMapping<I,O> extends Function<I, Iterable<? extends O>> {
    @Override
    Iterable<? extends O> apply(I value);
}
