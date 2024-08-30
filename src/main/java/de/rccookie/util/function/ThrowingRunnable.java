package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingRunnable<T extends Throwable> {
    void run() throws T;
}
