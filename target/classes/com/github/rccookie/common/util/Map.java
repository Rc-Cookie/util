package com.github.rccookie.common.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.github.rccookie.common.geometry.Vector2D;

public class Map<T> implements Cloneable, Iterable<T> {
    
    private final Plane<List<T>> grid;

    public Map() {
        grid = new Plane<>();
    }

    public Map(Map<T> map) {
        grid = new Plane<List<T>>(map.grid);
    }


    @Override
    protected Map<T> clone() {
        return new Map<>(this);
    }

    @Override
    public Iterator<T> iterator() {
        return getElements().iterator();
    }



    public void set(T element, Vector2D location) {
        Vector2D oldLocation = getLocation(element);
        if(location.equals(oldLocation)) return;
        if(oldLocation == null) return;
        grid.get(oldLocation).remove(element);
        checkNullAndSet(element, location);
    }

    private void checkNullAndSet(T element, Vector2D location) {
        if(location == null) return;
        List<T> elementsAtLoc = grid.get(location);
        if(elementsAtLoc == null) grid.set(location, new ArrayList<>());
        grid.get(location).add(element);
    }



    public synchronized Vector2D getLocation(T element) {
        getLocationVector = null;
        grid.forEachExact(e -> {
            if(e == null) return;
            if(e.value.contains(element)) getLocationVector = e.location();
        });
        return getLocationVector;
    }
    private Vector2D getLocationVector;





    public List<T> getElements() {
        List<T> elements = new ArrayList<>();
        for(List<T> elementsAtLoc : grid.all()) elements.addAll(elementsAtLoc);
        return elements;
    }

    public void clear() {
        grid.clear();
    }
}
