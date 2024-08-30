package de.rccookie.util.persistent;

import java.util.concurrent.locks.Lock;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

public abstract class AbstractPersistentData<T> implements PersistentData<T> {

    protected abstract T data();

    @Override
    public String toString() {
        return readLocked(Object::toString);
    }

    @Override
    public <R> R readLocked(Function<? super T, ? extends R> code) {
        Lock lock = lock().readLock();
        lock.lock();
        try {
            return code.apply(data());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void doReadLocked(Consumer<? super T> code) {
        Lock lock = lock().readLock();
        lock.lock();
        try {
            code.accept(data());
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <R> R writeLocked(Function<? super T, ? extends R> code) {
        Lock lock = lock().writeLock();
        lock.lock();
        try {
            R res = code.apply(data());
            markDirty();
            return res;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean testWriteLocked(Predicate<? super T> code) {
        Lock lock = lock().writeLock();
        lock.lock();
        try {
            if(!code.test(data()))
                return false;
            markDirty();
            return true;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void doWriteLocked(Consumer<? super T> code) {
        Lock lock = lock().writeLock();
        lock.lock();
        try {
            code.accept(data());
            markDirty();
        } finally {
            lock.unlock();
        }
    }
}
