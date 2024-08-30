package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingLongFunction<R, T extends Throwable> {
    R apply(long x) throws T;
}
