package com.github.rccookie.util;

public class Wrapper<T> extends AbstractWrapper {

    public T value;

    public Wrapper() { }

    public Wrapper(T value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
