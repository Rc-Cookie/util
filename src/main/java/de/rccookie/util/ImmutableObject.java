package de.rccookie.util;

public interface ImmutableObject {

    default UnsupportedOperationException newException() {
        return new ViewModificationException(this);
    }
}
