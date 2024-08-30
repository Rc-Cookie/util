package de.rccookie.util;

public abstract class AbstractImmutableCollection<T> implements ImmutableCollection<T> {

    @Override
    public String toString() {
        return Utils.toString(this);
    }
}
