package de.rccookie.util;

public class BoolWrapper extends AbstractWrapper {

    public boolean value;

    public BoolWrapper() { }

    public BoolWrapper(boolean value) {
        this.value = value;
    }

    public boolean get() {
        return value;
    }

    public void set(boolean value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
