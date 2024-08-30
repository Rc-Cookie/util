package de.rccookie.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface IterableMap<K,V> extends Map<K,V>, Iterable<K> {

    @NotNull
    ListStream<Entry<K,V>> stream();

    @Contract(value = "_ -> new", pure = true)
    static <K,V> @NotNull IterableMap<K,V> of(@NotNull Map<K,V> map) {
        Arguments.checkNull(map, "map");
        return new IterableMap<>() {
            @Override
            public @NotNull ListStream<Entry<K, V>> stream() {
                return ListStream.of(map.entrySet());
            }

            @NotNull
            @Override
            public Iterator<K> iterator() {
                return map.keySet().iterator();
            }

            @Override
            public int size() {
                return map.size();
            }

            @Override
            public boolean isEmpty() {
                return map.isEmpty();
            }

            @Override
            public boolean containsKey(Object key) {
                return map.containsKey(key);
            }

            @Override
            public boolean containsValue(Object value) {
                return map.containsValue(value);
            }

            @Override
            public V get(Object key) {
                return map.get(key);
            }

            @Nullable
            @Override
            public V put(K key, V value) {
                return map.put(key, value);
            }

            @Override
            public V remove(Object key) {
                return map.remove(key);
            }

            @Override
            public void putAll(@NotNull Map<? extends K, ? extends V> m) {
                map.putAll(m);
            }

            @Override
            public void clear() {
                map.clear();
            }

            @NotNull
            @Override
            public Set<K> keySet() {
                return map.keySet();
            }

            @NotNull
            @Override
            public Collection<V> values() {
                return map.values();
            }

            @NotNull
            @Override
            public Set<Entry<K, V>> entrySet() {
                return map.entrySet();
            }

            @Override
            public boolean equals(Object obj) {
                return map.equals(obj);
            }

            @Override
            public int hashCode() {
                return map.hashCode();
            }

            @Override
            public String toString() {
                return map.toString();
            }
        };
    }
}
