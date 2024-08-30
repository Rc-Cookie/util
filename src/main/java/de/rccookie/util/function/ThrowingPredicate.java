package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingPredicate<I, T extends Throwable> {
    boolean test(I x) throws T;
}
