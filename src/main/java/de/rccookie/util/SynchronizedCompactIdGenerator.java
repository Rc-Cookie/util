package de.rccookie.util;

public class SynchronizedCompactIdGenerator extends CompactIdGenerator {
    public SynchronizedCompactIdGenerator(int firstId) {
        super(firstId);
    }

    public SynchronizedCompactIdGenerator() { }

    @Override
    public synchronized int allocate() {
        return super.allocate();
    }

    @Override
    public synchronized int peek() {
        return super.peek();
    }

    @Override
    public synchronized void free(int id) {
        super.free(id);
    }

    @Override
    public synchronized int usedCount() {
        return super.usedCount();
    }

    @Override
    public synchronized int range() {
        return super.range();
    }

    @Override
    public synchronized boolean isFree(int id) {
        return super.isFree(id);
    }
}
