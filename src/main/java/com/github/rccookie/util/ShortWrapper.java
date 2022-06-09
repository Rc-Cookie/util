package com.github.rccookie.util;

public class ShortWrapper extends AbstractWrapper {

    public short value;

    public ShortWrapper() { }

    public ShortWrapper(short value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
