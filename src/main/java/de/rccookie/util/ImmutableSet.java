package de.rccookie.util;

import java.util.Collection;
import java.util.Set;

import org.jetbrains.annotations.NotNull;

public interface ImmutableSet<T> extends ImmutableCollection<T>, Set<T> {

    @Override
    default void clear() {
        ImmutableCollection.super.clear();
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        return ImmutableCollection.super.retainAll(c);
    }

    @Override
    default boolean add(T t) {
        return ImmutableCollection.super.add(t);
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
    default Object @NotNull [] toArray() {
        return ImmutableCollection.super.toArray();
    }

    @Override
    default boolean remove(Object o) {
        return ImmutableCollection.super.remove(o);
    }
}
