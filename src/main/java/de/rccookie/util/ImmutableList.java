package de.rccookie.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.UnaryOperator;

import org.jetbrains.annotations.NotNull;

public interface ImmutableList<T> extends ImmutableCollection<T>, List<T> {

    @Override
    default boolean addAll(int index, @NotNull Collection<? extends T> c) {
        throw newException();
    }

    @Override
    default void replaceAll(UnaryOperator<T> operator) {
        throw newException();
    }

    @Override
    default void sort(Comparator<? super T> c) {
        throw newException();
    }

    @Override
    default T set(int index, T element) {
        throw newException();
    }

    @Override
    default void add(int index, T element) {
        throw newException();
    }

    @Override
    default T remove(int index) {
        throw newException();
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        return ImmutableCollection.super.retainAll(c);
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        return ImmutableCollection.super.addAll(c);
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        return ImmutableCollection.super.removeAll(c);
    }

    @Override
    default void clear() {
        ImmutableCollection.super.clear();
    }

    @Override
    default boolean add(T t) {
        return ImmutableCollection.super.add(t);
    }

    @Override
    default Object @NotNull [] toArray() {
        return ImmutableCollection.super.toArray();
    }

    @Override
    default boolean remove(Object o) {
        return ImmutableCollection.super.remove(o);
    }
}
