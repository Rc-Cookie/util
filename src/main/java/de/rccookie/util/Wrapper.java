package de.rccookie.util;

public class Wrapper<T> extends AbstractWrapper {

    public T value;

    public Wrapper() { }

    public Wrapper(T value) {
        this.value = value;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
    }

    @Override
    T value() {
        return value;
    }
}
