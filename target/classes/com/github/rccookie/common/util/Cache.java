package com.github.rccookie.common.util;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class Cache<K, V> implements Map<K, V> {

    private long cacheDuration;

    private final HashMap<K,CachedItem<V>> cachedItems = new HashMap<>();





    public Cache(long cacheDuration) {
        setCacheDuration(cacheDuration);
    }

    public Cache(long cacheDuration, Map<? extends K, ? extends V> items) {
        this(cacheDuration);
        putAll(items);
    }





    @Override
    public int size() {
        return keySet().size();
    }

    @Override
    public boolean isEmpty() {
        return size() == 0;
    }

    @Override
    public boolean containsKey(Object key) {
        return keySet().contains(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return values().contains(value);
    }

    @Override
    public V get(Object key) {
        CachedItem<V> item = cachedItems.get(key);
        if(item == null) return null;
        V value = item.get();
        if(value == null) {
            cachedItems.remove(key);
            return null;
        }
        return value;
    }

    protected CachedItem<V> getCache(Object key) {
        return cachedItems.get(key);
    }

    @Override
    public V put(K key, V value) {
        CachedItem<V> cachedItem = getCache(key);
        if(cachedItem == null) {
            cachedItems.put(key, new CachedItem<V>(cacheDuration, value));
            return null;
        }
        return cachedItem.set(value);
    }

    public V getOrPutNew(K key, Supplier<V> newValueGenerator) {
        CachedItem<V> cachedItem = getCache(key);
        if(cachedItem == null) {
            V newValue = newValueGenerator.get();
            put(key, newValue);
            return newValue;
        }
        return cachedItem.getOrSetNew(newValueGenerator);
    }

    @Override
    public V remove(Object key) {
        V oldValue = get(key);
        cachedItems.remove(key);
        return oldValue;
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> map) {
        for(Map.Entry<? extends K, ? extends V> entry : map.entrySet()) put(entry.getKey(), entry.getValue());
    }

    @Override
    public void clear() {
        cachedItems.clear();
    }

    @Override
    public Set<K> keySet() {
        Set<K> keys = new HashSet<K>();
        for(K key : cachedItems.keySet()) if(get(key) != null) keys.add(key);
        return keys;
    }

    @Override
    public Collection<V> values() {
        return keySet().stream().map(key -> get(key)).collect(Collectors.toList());
    }

    @Override
    public Set<java.util.Map.Entry<K, V>> entrySet() {
        return keySet().stream().map(key -> new Entry<K,V>(key, getCache(key))).collect(Collectors.toSet());
    }




    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
        for(CachedItem<V> value : cachedItems.values()) value.setCacheDuration(cacheDuration);
    }

    public long getCacheDuration() {
        return cacheDuration;
    }



    
    private static class Entry<K, V> implements Map.Entry<K, V> {

        private final K key;
        private CachedItem<V> value;

        Entry(K key, CachedItem<V> value) {
            this.key = key;
            this.value = value;
        }

        @Override
        public K getKey() {
            return key;
        }

        @Override
        public V getValue() {
            return value.get();
        }

        @Override
        public V setValue(V value) {
            V previous = this.value.get();
            this.value.set(value);
            return previous;
        }
    }
}
