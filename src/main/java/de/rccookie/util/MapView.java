package de.rccookie.util;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface MapView<K,V> extends Map<K,V> {

    Map<K,V> viewedMap();

    @Override
    default int size() {
        return viewedMap().size();
    }

    @Override
    default boolean isEmpty() {
        return viewedMap().isEmpty();
    }

    @Override
    default boolean containsKey(Object key) {
        return viewedMap().containsKey(key);
    }

    @Override
    default boolean containsValue(Object value) {
        return viewedMap().containsValue(value);
    }

    @Override
    default V get(Object key) {
        return viewedMap().get(key);
    }

    @Nullable
    @Override
    default V put(K key, V value) {
        return viewedMap().put(key, value);
    }

    @Override
    default V remove(Object key) {
        return viewedMap().remove(key);
    }

    @Override
    default void putAll(@NotNull Map<? extends K, ? extends V> m) {
        viewedMap().putAll(m);
    }

    @Override
    default void clear() {
        viewedMap().clear();
    }

    @NotNull
    @Override
    default Set<K> keySet() {
        return viewedMap().keySet();
    }

    @NotNull
    @Override
    default Collection<V> values() {
        return viewedMap().values();
    }

    @NotNull
    @Override
    default Set<Entry<K, V>> entrySet() {
        return viewedMap().entrySet();
    }

    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return viewedMap().getOrDefault(key, defaultValue);
    }

    @Override
    default void forEach(BiConsumer<? super K, ? super V> action) {
        viewedMap().forEach(action);
    }

    @Override
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        viewedMap().replaceAll(function);
    }

    @Nullable
    @Override
    default V putIfAbsent(K key, V value) {
        return viewedMap().putIfAbsent(key, value);
    }

    @Override
    default boolean remove(Object key, Object value) {
        return viewedMap().remove(key, value);
    }

    @Override
    default boolean replace(K key, V oldValue, V newValue) {
        return viewedMap().replace(key, oldValue, newValue);
    }

    @Nullable
    @Override
    default V replace(K key, V value) {
        return viewedMap().replace(key, value);
    }

    @Override
    default V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        return viewedMap().computeIfAbsent(key, mappingFunction);
    }

    @Override
    default V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return viewedMap().computeIfPresent(key, remappingFunction);
    }

    @Override
    default V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return viewedMap().compute(key, remappingFunction);
    }

    @Override
    default V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return viewedMap().merge(key, value, remappingFunction);
    }
}
