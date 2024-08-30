package de.rccookie.util;

import java.util.Collection;
import java.util.function.Predicate;

import org.jetbrains.annotations.NotNull;

public interface ImmutableCollection<T> extends ImmutableObject, Collection<T> {
    @Override
    default boolean removeIf(Predicate<? super T> filter) {
        throw newException();
    }

    @Override
    default Object @NotNull[] toArray() {
        return toArray(new Object[0]);
    }

    @Override
    default boolean add(T t) {
        throw newException();
    }

    @Override
    default boolean remove(Object o) {
        throw newException();
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        throw newException();
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        throw newException();
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        throw newException();
    }

    @Override
    default void clear() {
        throw newException();
    }
}
