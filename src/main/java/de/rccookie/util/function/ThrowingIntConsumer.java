package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingIntConsumer<T extends Throwable> {
    void accept(int x) throws T;
}
