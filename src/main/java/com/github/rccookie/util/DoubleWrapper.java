package com.github.rccookie.util;

public class DoubleWrapper extends AbstractWrapper {

    public double value;

    public DoubleWrapper() { }

    public DoubleWrapper(double value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
