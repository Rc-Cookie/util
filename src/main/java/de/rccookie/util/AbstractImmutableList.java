package de.rccookie.util;

public abstract class AbstractImmutableList<T> extends AbstractImmutableCollection<T> implements ImmutableList<T> {

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return Utils.hashCode(this);
    }
}
