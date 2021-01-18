package com.github.rccookie.common.util;

import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.github.rccookie.common.geometry.Vector2D;


/**
 * A grid represents a ordered 2-dimensional collection of elements.
 * <p>Grids may be infinite or have a fixed size. The may allow or
 * dissallow {@code null} values.
 * <p>In every location of the grid there can be exactly one value.
 * <p>Some implementation may allow floating point 'coordinates'.
 */
public interface Grid<T> extends Iterable<T>, Cloneable {
    

    /**
     * Returns the element at the specified location in the grid.
     * 
     * @param row The row of the element
     * @param column The column of the element
     * @return The element at the specified location
     */
    public T get(int row, int column);

    /**
     * Sets the element at the specified location to the given value
     * and returns the old value.
     * 
     * @param row The row of the element
     * @param column The column of the element
     * @param value The new value
     * @return The old value
     */
    public T set(int row, int column, T value);

    /**
     * Clears all elements from the grid.
     */
    public void clear();


    /**
     * Returns all elements in the specified row in proper sequence.
     * 
     * @param index The index if the row
     * @return A list containing all elements in the specified row
     */
    public List<T> row(int index);
    
    /**
     * Returns all elements in the specified column in proper sequence.
     * 
     * @param index The index if the column
     * @return A list containing all elements in the specified column
     */
    public List<T> column(int index);


    /**
     * Runs the given action for each element in the grid with a
     * {@code GridElement} for the current object passed as argument.
     * 
     * @param action The action to execute for each element
     */
    public void forEachLoc(Consumer<GridElement<T>> action);


    /**
     * Returns all elements in the grid in proper sequence.
     * 
     * @return All elements
     */
    public List<T> all();

    /**
     * Returns all elements for which the given predicate returns {@code true}
     * when as argument a {@code GridElement} for the current object is passed.
     * 
     * @param condition The condition under which an element is conform
     * @return All elements for which the condition returns {@code true}
     */
    public List<T> all(Predicate<GridElement<T>> condition);

    /**
     * Returns one element in the grid for which the given predicate returns
     * {@code true} when as argument a {@code GridElement} for the current
     * object is passed. If no elements conform the condition, {@code null}
     * will be returned.
     * 
     * @param condition The condition under which an element is conform
     * @return One element from the grid, or {@code null}
     */
    public GridElement<T> oneElement(Predicate<GridElement<T>> condition);

    /**
     * Returns one element in the grid for which the given predicate returns
     * {@code true} when as argument a {@code GridElement} for the current
     * object is passed. If no elements conform the condition, {@code null}
     * will be returned. The difference to {@code oneElement} is that here
     * only the value itself will be returned.
     * <p>By delfault this works as shown:
     * <pre>
     * GridElement<T> oneElement = oneElement(condition);
     * return oneElement != null ? oneElement.value : null;
     * </pre>
     * 
     * @param condition The condition under which an element is conform
     * @return One value from the grid, or {@code null}
     */
    public default T one(Predicate<GridElement<T>> condition) {
        GridElement<T> oneElement = oneElement(condition);
        return oneElement != null ? oneElement.value : null;
    }

    /**
     * Returns if the given condition returns {@code true} for any element
     * in the grid.
     * <p>By delfault this returns:
     * <pre>one(condition) != null</pre>
     * 
     * @param condition The condition under which an element is conform
     * @return Weather the given condition is {@code true} for any element
     *         in the grid
     */
    public default boolean any(Predicate<GridElement<T>> condition) {
        return one(condition) != null;
    }

    /**
     * Returns weather the grid contains the specified element.
     * <p>By delfault this returns:
     * <pre>any(e -> Objects.equals(e.value, element))</pre>
     * 
     * @param element The element to check for
     * @return {@code true} if there is at least one reference to the
     *         given element in the grid
     */
    public default boolean contains(T element) {
        return any(e -> Objects.equals(e.value, element));
    }

    /**
     * Returns the first location of the given element in the table as
     * a {@code Vector2D}, or {@code null} of it is not contained.
     * <p>By delfault this does:
     * <pre>
     * GridElement<T> oneElement = oneElement(e -> Objects.equals(e.value, element));
     * return oneElement != null ? oneElement.location() : null;
     * </pre>
     * 
     * @param element The element to get the location of
     * @return The elements first location, or {@code null}
     */
    public default Vector2D locationOf(T element) {
        GridElement<T> oneElement = oneElement(e -> Objects.equals(e.value, element));
        return oneElement != null ? oneElement.location() : null;
    }

    /**
     * Returns weather the grid is empty.
     * <p>By delfault this returns:
     * <pre>!any(element -> element.value != null)</pre>
     * 
     * @return {@code true} if the grid only contains {@code null} values
     *         if any
     */
    public default boolean isEmpty() {
        return !any(element -> element.value != null);
    }







    /**
     * Returns an iterator that iterates over all elements in the grid.
     * This may (not neccecarilly) contain {@code null} values.
     * <p>By default this returns:
     * <pre>all().iterator();</pre>
     */
    @Override
    default Iterator<T> iterator() {
        return all().iterator();
    }

    
    /**
     * Returns a copy of this grid. Weather the actual elements in the grid
     * are cloned too may differ between implementations.
     * 
     * @return A clone of this grid
     */
    public Grid<T> clone();


    /**
     * Represents an element in a table. Contains its value and its location in the table.
     * Used to give information about an elements state.
     */
    public static class GridElement<T> {

        /**
         * The elements value.
         */
        public final T value;

        /**
         * The elements location in the table.
         */
        private final int row, column;

        /**
         * Creates a new table element with the specified content.
         * 
         * @param value
         * @param row
         * @param column
         */
        protected GridElement(T value, int row, int column) {
            this.value = value;
            this.row = row;
            this.column = column;
        }

        public int row() { return row; }
        public int column() { return column; }

        public Vector2D location() {
            return new Vector2D(row, column);
        }
    }
}
