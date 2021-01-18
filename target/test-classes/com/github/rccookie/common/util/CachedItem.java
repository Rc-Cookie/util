package com.github.rccookie.common.util;

import java.util.function.Supplier;

public class CachedItem<V> {

    private long cacheDuration;
    
    private V value;
    private long cacheTimeStamp;



    public CachedItem(long cacheDuration) {
        setCacheDuration(cacheDuration);
    }

    public CachedItem(long cacheDuration, V value) {
        this(cacheDuration);
        set(value);
    }



    public V set(V value) {
        V oldValue = get();
        this.value = value;
        if(value != null) cacheTimeStamp = System.currentTimeMillis();
        return oldValue;
    }

    public V get() {
        if(value == null) return null;
        if(System.currentTimeMillis() - cacheTimeStamp > cacheDuration) value = null;
        return value;
    }

    public V getOrSetNew(Supplier<V> newValueGenerator) {
        V current = get();
        if(current == null) set(newValueGenerator.get());
        return value;
    }



    public void setCacheDuration(long cacheDuration) {
        this.cacheDuration = cacheDuration;
    }

    public long getCacheDuration() {
        return cacheDuration;
    }
}
