package com.github.rccookie.util;

public class IntWrapper extends AbstractWrapper {

    public int value;

    public IntWrapper() { }

    public IntWrapper(int value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
