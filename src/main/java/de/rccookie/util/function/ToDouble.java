package de.rccookie.util.function;

@FunctionalInterface
public interface ToDouble<I> {

    double asDouble(I o);
}
