package com.github.rccookie.common.util;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.rccookie.common.geometry.Vector2D;


/**
 * A plane represents an infinite 2-dimensional area with not more that one object per point.
 * Planes act as coordinate systems and support {@code double} coordinates.
 */
public class Plane<T> implements InfiniteGrid<T> {

    /**
     * The comperator used to sort the locations of the entrys.
     */
    static final Comparator<Vector2D> COMPARATOR = (a, b) -> {
        if(a == null) return b != null ? -1 : 0;
        if(b == null) return 1;
        if(a.x() != b.x()) return a.x() > b.x() ? 1 : -1;
        if(a.y() != b.y()) return a.y() > b.y() ? 1 : -1;
        return 0;
    };


    /**
     * Stores the objects with their locations.
     */
    private TreeMap<Vector2D, T> elements = new TreeMap<Vector2D, T>(COMPARATOR);

    
    /**
     * Creates a new empty plane.
     */
    public Plane() {}

    /**
     * Creates a new plane with all the elements from the given Plane.
     * @param map
     */
    public Plane(Plane<? extends T> map) {
        super();
        elements.putAll(map.elements);
    }




    @Override
    public T get(int x, int y) {
        return get((double)x, (double)y);
    }
    
    /**
     * Returns the element at the specified location in the grid.
     * @param x
     * @param y
     * @return The element at the specified location
     */
    public T get(double x, double y) {
        return get(new Vector2D(x, y));
    }

    /**
     * Returns the element at the specified location in the grid.
     * @param loc The location of the element, as vector
     * @return The element at the specified location
     */
    public T get(Vector2D loc) {
        return elements.get(loc);
    }



    /**
     * Sets the element at the specified location to the given value
     * and returns the old value.
     * 
     * @param x The x coordinate of the element
     * @param y The y coordinate of the element
     * @param value The new value
     * @return The old value
     */
    @Override
    public T set(int x, int y, T value) {
        return set((double)x, (double)y, value);
    }

    /**
     * Sets the element at the specified location to the given value
     * and returns the old value.
     * 
     * @param x The x coordinate of the element
     * @param y The y coordinate of the element
     * @param value The new value
     * @return The old value
     */
    public T set(double x, double y, T value) {
        return set(new Vector2D(x, y), value);
    }

    /**
     * Sets the element at the specified location to the given value
     * and returns the old value.
     * 
     * @param loc the location of the element, as vector
     * @param value The new value
     * @return The old value
     */
    public T set(Vector2D loc, T value) {
        T old = get(loc);
        elements.put(loc, value);
        return old;
    }



    @Override
    public void clear() {
        elements.clear();
    }


    
    /**
     * Returns all elements in the specified row in proper sequence.
     * 
     * @param y The y coordinate of the row
     * @return All elements in the specified row
     */
    @Override
    public List<T> row(int y) {
        return row((double)y);
    }

    /**
     * Returns all elements in the specified row in proper sequence.
     * 
     * @param y The y coordinate of the row
     * @return All elements in the specified row
     */
    public List<T> row(double y) {
        List<T> row = new ArrayList<>();
        for(Entry<Vector2D, T> entry : elements.entrySet()) {
            if(entry.getKey().y() == y) row.add(entry.getValue());
        }
        return row;
    }



    /**
     * Returns all elements in the specified column in proper sequence.
     * 
     * @param x The x coordinate of the column
     * @return All elements in the specified column
     */
    @Override
    public List<T> column(int x) {
        return column((double)x);
    }

    /**
     * Returns all elements in the specified column in proper sequence.
     * 
     * @param x The x coordinate of the column
     * @return All elements in the specified column
     */
    public List<T> column(double x) {
        List<T> column = new ArrayList<>();
        for(Entry<Vector2D, T> entry : elements.entrySet()) {
            if(entry.getKey().x() == x) column.add(entry.getValue());
        }
        return column;
    }



    /**
     * Runs the given action for each element in the grid with an
     * {@code ExactGridElement} for the current object passed as argument.
     * 
     * @param action The action to execute for each element
     */
    @Override
    public void forEachLoc(Consumer<GridElement<T>> action) {
        for (Entry<Vector2D, T> entry : elements.entrySet()) {
            action.accept(new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y()));
        }
    }

    /**
     * Runs the given action for each element in the grid with an
     * {@code ExactGridElement} for the current object passed as argument.
     * 
     * @param action The action to execute for each element
     */
    public void forEachExact(Consumer<ExactGridElement<T>> action) {
        for (Entry<Vector2D, T> entry : elements.entrySet()) {
            action.accept(new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y()));
        }
    }



    @Override
    public List<T> all() {
        return new ArrayList<>(elements.values());
    }



    /**
     * Returns all elements for which the given predicate returns {@code true}
     * when as argument an {@code ExactGridElement} for the current object is passed.
     * 
     * @param condition The condition under which an element is conform
     * @return All elements for which the condition returns {@code true}
     */
    @Override
    public List<T> all(Predicate<GridElement<T>> condition) {
        List<T> fullfilling = new ArrayList<>();
        for (Entry<Vector2D, T> entry : elements.entrySet()) {
            if(condition.test(new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y())))
                fullfilling.add(entry.getValue());
        }
        return fullfilling;
    }

    /**
     * Returns all elements for which the given predicate returns {@code true}
     * when as argument an {@code ExactGridElement} for the current object is passed.
     * 
     * @param condition The condition under which an element is conform
     * @return All elements for which the condition returns {@code true}
     */
    public List<T> allExact(Predicate<ExactGridElement<T>> condition) {
        List<T> fullfilling = new ArrayList<>();
        for (Entry<Vector2D, T> entry : elements.entrySet()) {
            if(condition.test(new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y())))
                fullfilling.add(entry.getValue());
        }
        return fullfilling;
    }



    /**
     * Returns one element in the grid for which the given predicate returns
     * {@code true} when as argument an {@code ExactGridElement} for the current
     * object is passed. If no elements conform the condition, {@code null}
     * will be returned.
     * 
     * @param condition The condition under which an element is conform
     * @return One element from the grid, or {@code null}
     */
    @Override
    public GridElement<T> oneElement(Predicate<GridElement<T>> condition) {
        for (Entry<Vector2D, T> entry : elements.entrySet()) {
            if(condition.test(new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y())))
                return new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y());
        }
        return null;
    }

    /**
     * Returns one element in the grid for which the given predicate returns
     * {@code true} when as argument an {@code ExactGridElement} for the current
     * object is passed. If no elements conform the condition, {@code null}
     * will be returned.
     * 
     * @param condition The condition under which an element is conform
     * @return One element from the grid, or {@code null}
     */
    public GridElement<T> oneExactElement(Predicate<ExactGridElement<T>> condition) {
        for (Entry<Vector2D, T> entry : elements.entrySet()) {
            if(condition.test(new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y())))
                return new ExactGridElement<T>(entry.getValue(), entry.getKey().x(), entry.getKey().y());
        }
        return null;
    }



    /**
     * Returns a shallow copy if this plane. The elements themselfes are not copied.
     */
    @Override
    public Plane<T> clone() {
        return new Plane<>(this);
    }



    /**
     * A variation of {@code GridElement} that supports {@code double} values.
     */
    public static class ExactGridElement<T> extends GridElement<T> {
        
        private final double x, y;

        public ExactGridElement(T value, double x, double y) {
            super(value, (int)x, (int)y);
            this.x = x;
            this.y = y;
        }

        public double x() { return x; }
        public double y() { return y; }

        @Override
        public Vector2D location() { return new Vector2D(x, y); }
    }
}
