package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingIntFunction<R, T extends Throwable> {
    R apply(int x) throws T;
}
