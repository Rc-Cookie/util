package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingConsumer<I, T extends Throwable> {
    void accept(I x) throws T;
}
