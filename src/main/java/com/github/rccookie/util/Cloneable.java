package com.github.rccookie.util;

import org.jetbrains.annotations.NotNull;

/**
 * Better version of {@link java.lang.Cloneable} that forces the class
 * to return an object of its own type and forbids the throwing of a
 * {@link CloneNotSupportedException} because that does not make any
 * sense, and forces public visibility.
 *
 * @param <T> The type of the class implementing the interface
 */
public interface Cloneable<T> extends java.lang.Cloneable {

    @NotNull
    T clone();
}
