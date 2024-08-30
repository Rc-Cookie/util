package de.rccookie.util;

public interface ActiveLock extends AutoCloseable {

    void unlock();

    @Override
    default void close() {
        unlock();
    }
}
