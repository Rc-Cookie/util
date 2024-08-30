package de.rccookie.util;

public class LongWrapper extends AbstractWrapper {

    public long value;

    public LongWrapper() { }

    public LongWrapper(long value) {
        this.value = value;
    }

    public long get() {
        return value;
    }

    public void set(long value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
