package de.rccookie.util;

public abstract class AbstractImmutableMap<K,V> implements ImmutableMap<K,V> {

    @Override
    public String toString() {
        return Utils.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return Utils.hashCode(this);
    }
}
