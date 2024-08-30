package de.rccookie.util.persistent;

import java.util.Collection;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface PersistentMap<K,V> extends Map<K,V>, PersistentData<Map<K,V>> {

    @Override
    default int size() {
        return readLocked(Map::size);
    }

    @Override
    default boolean isEmpty() {
        return readLocked(Map::isEmpty);
    }

    @Override
    default boolean containsKey(Object key) {
        return readLocked(m -> m.containsKey(key));
    }

    @Override
    default boolean containsValue(Object value) {
        return readLocked(m -> m.containsValue(value));
    }

    @Override
    default V get(Object key) {
        return readLocked(m -> m.get(key));
    }

    @Nullable
    @Override
    default V put(K key, V value) {
        return writeLocked(m -> m.put(key, value));
    }

    @Override
    default V remove(Object key) {
        return writeLocked(m -> m.remove(key));
    }

    @Override
    default void putAll(@NotNull Map<? extends K, ? extends V> m) {
        if(!m.isEmpty())
            doWriteLocked(map -> map.putAll(m));
    }

    @Override
    default void clear() {
        testWriteLocked(m -> {
            if(m.isEmpty())
                return false;
            m.clear();
            return true;
        });
    }

    @NotNull
    @Override
    default Set<K> keySet() {
        return new PersistentSetView<>(this, Map::keySet);
    }

    @NotNull
    @Override
    default Collection<V> values() {
        return new PersistentCollectionView<>(this, Map::values);
    }

    @NotNull
    @Override
    default Set<Entry<K, V>> entrySet() {
        return new PersistentSetView<>(this, Map::entrySet);
    }

    @Override
    default V getOrDefault(Object key, V defaultValue) {
        return readLocked(m -> m.getOrDefault(key, defaultValue));
    }

    @Override
    default void forEach(BiConsumer<? super K, ? super V> action) {
        doReadLocked(m -> m.forEach(action));
    }

    @Override
    default void replaceAll(BiFunction<? super K, ? super V, ? extends V> function) {
        doWriteLocked(m -> m.replaceAll(function));
    }

    @Nullable
    @Override
    default V putIfAbsent(K key, V value) {
        return writeLocked(m -> m.putIfAbsent(key, value));
    }

    @Override
    default boolean remove(Object key, Object value) {
        return testWriteLocked(m -> m.remove(key, value));
    }

    @Override
    default boolean replace(K key, V oldValue, V newValue) {
        return testWriteLocked(m -> m.replace(key, oldValue, newValue));
    }

    @Nullable
    @Override
    default V replace(K key, V value) {
        return writeLocked(m -> m.replace(key, value));
    }

    @Override
    default V computeIfAbsent(K key, @NotNull Function<? super K, ? extends V> mappingFunction) {
        return writeLocked(m -> m.computeIfAbsent(key, mappingFunction));
    }

    @Override
    default V computeIfPresent(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return writeLocked(m -> m.computeIfPresent(key, remappingFunction));
    }

    @Override
    default V compute(K key, @NotNull BiFunction<? super K, ? super V, ? extends V> remappingFunction) {
        return writeLocked(m -> m.compute(key, remappingFunction));
    }

    @Override
    default V merge(K key, @NotNull V value, @NotNull BiFunction<? super V, ? super V, ? extends V> remappingFunction) {
        return writeLocked(m -> m.merge(key, value, remappingFunction));
    }
}
