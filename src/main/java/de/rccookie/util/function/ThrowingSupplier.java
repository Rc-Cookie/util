package de.rccookie.util.function;

@FunctionalInterface
public interface ThrowingSupplier<R, T extends Throwable> {
    R get() throws T;
}
