package com.github.rccookie.util;

public class ByteWrapper extends AbstractWrapper {

    public byte value;

    public ByteWrapper() { }

    public ByteWrapper(byte value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
