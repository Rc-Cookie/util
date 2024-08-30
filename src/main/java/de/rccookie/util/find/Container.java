package de.rccookie.util.find;

import java.util.HashMap;
import java.util.Map;

public class Container {

    private final Map<Object, Element> elements = new HashMap<>();

    public void makeSet(Object o) {
        elements.put(o, UnionFind.makeSet());
    }

    public Object find(Object x) {
        return UnionFind.find(elements.get(x));
    }

    public void union(Object x, Object y) {
        UnionFind.union(elements.get(x), elements.get(y));
    }
}
