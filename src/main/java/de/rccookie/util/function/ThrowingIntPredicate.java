package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingIntPredicate<T extends Throwable> {
    boolean test(int x) throws T;
}
