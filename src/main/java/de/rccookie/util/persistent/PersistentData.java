package de.rccookie.util.persistent;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import de.rccookie.util.ActiveLock;

public interface PersistentData<T> {

    void markDirty();

    void reload();

    ReadWriteLock lock();

    default ActiveLock lockRead() {
        Lock lock = lock().readLock();
        lock.lock();
        return lock::unlock;
    }

    default ActiveLock lockWrite() {
        Lock lock = lock().readLock();
        lock.lock();
        return lock::unlock;
    }

    default <R> R readLocked(Supplier<? extends R> code) {
        Lock lock = lock().readLock();
        lock.lock();
        try {
            return code.get();
        } finally {
            lock.unlock();
        }
    }

    default void doReadLocked(Runnable code) {
        Lock lock = lock().readLock();
        lock.lock();
        try {
            code.run();
        } finally {
            lock.unlock();
        }
    }

    default <R> R writeLocked(Supplier<? extends R> code) {
        Lock lock = lock().writeLock();
        lock.lock();
        try {
            R res = code.get();
            markDirty();
            return res;
        } finally {
            lock.unlock();
        }
    }

    default boolean testWriteLocked(BooleanSupplier code) {
        Lock lock = lock().writeLock();
        lock.lock();
        try {
            if(!code.getAsBoolean())
                return false;
            markDirty();
            return true;
        } finally {
            lock.unlock();
        }
    }

    default void doWriteLocked(Runnable code) {
        Lock lock = lock().writeLock();
        lock.lock();
        try {
            code.run();
            markDirty();
        } finally {
            lock.unlock();
        }
    }

    <R> R readLocked(Function<? super T, ? extends R> code);

    void doReadLocked(Consumer<? super T> code);

    <R> R writeLocked(Function<? super T, ? extends R> code);

    boolean testWriteLocked(Predicate<? super T> code);

    void doWriteLocked(Consumer<? super T> code);
}
