package de.rccookie.util;

import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ImmutableMap<K,V> extends ImmutableObject, Map<K,V> {

    @Nullable
    @Override
    default V put(K key, V value) {
        throw newException();
    }

    @Override
    default V remove(Object key) {
        throw newException();
    }

    @Override
    default void putAll(@NotNull Map<? extends K, ? extends V> m) {
        throw newException();
    }

    @Override
    default void clear() {
        throw newException();
    }

    @Override
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        throw newException();
    }

    @Nullable
    @Override
    default V putIfAbsent(K key, V value) {
        throw newException();
    }

    @Override
    default boolean remove(Object key, Object value) {
        throw newException();
    }

    @Override
    default boolean replace(K key, V oldValue, V newValue) {
        throw newException();
    }

    @Nullable
    @Override
    default V replace(K key, V value) {
        throw newException();
    }

    @Override
    default V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        throw newException();
    }

    @Override
    default V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw newException();
    }

    @Override
    default V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        throw newException();
    }

    @Override
    default V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        throw newException();
    }
}
