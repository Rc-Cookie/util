package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingLongConsumer<T extends Throwable> {
    void accept(long x) throws T;
}
