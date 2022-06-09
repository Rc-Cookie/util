package com.github.rccookie.util;

public class BoolWrapper extends AbstractWrapper {

    public boolean value;

    public BoolWrapper() { }

    public BoolWrapper(boolean value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
