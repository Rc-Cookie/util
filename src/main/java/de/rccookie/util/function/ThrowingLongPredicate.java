package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingLongPredicate<T extends Throwable> {
    boolean test(long x) throws T;
}
