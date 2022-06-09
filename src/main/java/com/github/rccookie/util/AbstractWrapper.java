package com.github.rccookie.util;

import java.util.Objects;

abstract class AbstractWrapper {

    abstract Object value();

    @Override
    public String toString() {
        return Objects.toString(value());
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(obj.getClass() != getClass()) return false;
        return Objects.equals(value(), ((AbstractWrapper) obj).value());
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(value());
    }
}
