package com.github.rccookie.common.util;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.stream.Collectors;

public abstract class TreeLine<N extends Number, T> implements Line<N, T> {

    private static final long serialVersionUID = -7361363665328415894L;
    
    private final TreeMap<N, T> map;

    public TreeLine() {
        map = new TreeMap<>(COMPARATOR);
    }

    public TreeLine(SortedMap<? extends N, ? extends T> map) {
        this.map = new TreeMap<N,T>(map);
    }

    public TreeLine(Map<? extends N, ? extends T> map) {
        this();
        putAll(map);
    }

    @Override
    public Comparator<? super N> comparator() {
        return map.comparator();
    }

    @Override
    public SortedMap<N, T> subMap(N fromIndex, N toIndex) {
        return map.subMap(fromIndex, toIndex);
    }

    @Override
    public SortedMap<N, T> headMap(N toIndex) {
        return map.headMap(toIndex);
    }

    @Override
    public SortedMap<N, T> tailMap(N fromIndex) {
        return map.tailMap(fromIndex);
    }

    @Override
    public N firstKey() {
        return map.firstKey();
    }

    @Override
    public N lastKey() {
        return map.lastKey();
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
    public boolean containsKey(Object index) {
        return map.containsKey(index);
    }

    @Override
    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    @Override
    public T get(Object index) {
        return map.get(index);
    }

    @Override
    public T put(N index, T value) {
        return map.put(index, value);
    }

    @Override
    public T remove(Object index) {
        return map.remove(index);
    }

    @Override
    public void putAll(Map<? extends N, ? extends T> m) {
        map.putAll(map);
    }

    @Override
    public void clear() {
        map.clear();
    }

    @Override
    public SortedSet<Entry<N, T>> entrySet() {
        return new TreeSet<>(map.entrySet().stream().collect(Collectors.toList()));
    }

    @Override
    public List<T> values() {
        return map.values().stream().collect(Collectors.toList());
    }

    @Override
    public SortedSet<N> keySet() {
        return new TreeSet<>(map.keySet().stream().collect(Collectors.toList()));
    }

    @Override
    protected abstract TreeLine<N,T> clone();

    @Override
    public String toString() {
        if(isEmpty()) return "{}";
        return '{' + entryStream().map(e -> new StringBuilder("[").append(e.getKey()).append("]: ").append(e.getValue()).toString()).collect(Collectors.joining(" - "));
    }
}
