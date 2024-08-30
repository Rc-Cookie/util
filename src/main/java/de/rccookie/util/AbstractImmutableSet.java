package de.rccookie.util;

public abstract class AbstractImmutableSet<T> extends AbstractImmutableCollection<T> implements ImmutableSet<T> {

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return Utils.hashCode(this);
    }
}
