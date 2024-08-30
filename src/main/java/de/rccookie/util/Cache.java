package de.rccookie.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class Cache<K, V> implements Map<K, V> {

    private final Map<K,V> data;
    private final Function<? super K,? extends V> generator;

    public Cache(@NotNull Function<? super K,? extends V> generator) {
        data = new HashMap<>();
        this.generator = Arguments.checkNull(generator, "generator");
    }

    public Cache(@NotNull Function<? super K,? extends V> generator, @NotNull Map<? extends K, ? extends V> map) {
        data = new HashMap<>(map);
        this.generator = Arguments.checkNull(generator, "generator");
    }

    @Override
    public int size() {
        return data.size();
    }

    @Override
    public boolean isEmpty() {
        return data.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return data.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return data.containsValue(value);
    }

    @SuppressWarnings("unchecked")
    @Override
    public V get(Object key) {
        return data.computeIfAbsent((K) key, generator);
    }

    @Nullable
    @Override
    public V put(K key, V value) {
        return data.put(key, value);
    }

    @Override
    public V remove(Object key) {
        return data.remove(key);
    }

    @Override
    public void putAll(@NotNull Map<? extends K, ? extends V> m) {
        data.putAll(m);
    }

    @Override
    public void clear() {
        data.clear();
    }

    @NotNull
    @Override
    public Set<K> keySet() {
        return data.keySet();
    }

    @NotNull
    @Override
    public Collection<V> values() {
        return data.values();
    }

    @NotNull
    @Override
    public Set<Entry<K, V>> entrySet() {
        return data.entrySet();
    }
}
