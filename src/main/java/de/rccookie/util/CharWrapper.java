package de.rccookie.util;

public class CharWrapper extends AbstractWrapper {

    public char value;

    public CharWrapper() { }

    public CharWrapper(char value) {
        this.value = value;
    }

    public char get() {
        return value;
    }

    public void set(char value) {
        this.value = value;
    }

    @Override
    Object value() {
        return value;
    }
}
