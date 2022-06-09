package com.github.rccookie.util;

public class FloatWrapper extends AbstractWrapper {

    public float value;

    public FloatWrapper() { }

    public FloatWrapper(float value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
