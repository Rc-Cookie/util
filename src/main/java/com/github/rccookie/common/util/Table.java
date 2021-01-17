package com.github.rccookie.common.util;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;


/**
 * The table represents a data structure that contains the specified data type in a table-ordered
 * system with predefined row and colomn size.
 * <p>Additionally, the class contains a lot of useful methods to interact with all content
 * similarly.
 */
public class Table<T> implements AbstractTable<T> {

    /**
     * The content stored in the table. As type {@code Object} because you cannot create arrays
     * of generic types.
     */
    private final Object[][] elements;


    /**
     * Creates an empty table with the specified dimensions.
     * 
     * @param rows The number of rows
     * @param columns The number of columns
     */
    public Table(int rows, int columns) {
        elements = new Object[rows][columns];
    }

    /**
     * Creates a table and initializes each table element with the given initializer.
     * 
     * @param rows The number of rows
     * @param columns The number of columns
     * @param initializer The initializer used to initialize the objects in the table
     */
    public Table(int rows, int columns, Function<GridElement<T>, T> initializer) {
        this(rows, columns);
        initialize(initializer);
    }

    /**
     * Creates a new table from the given one.
     * <p>{@code new Grid(gridInstance)} does exactly the same as invoking {@code gridInstance.clone()}.
     * 
     * @param table The table to create a clone from
     */
    public Table(Table<? extends T> table) {
        this(table.columnCount(), table.rowCount());
        table.forEachLoc(element -> elements[element.row()][element.column()] = element.value);
    }

    /**
     * Returns a shallow copy of this table. The actual elements will not be cloned.
     * <p>
     * {@code new Grid(gridInstance)} does exactly the same as invoking
     * {@code gridInstance.clone()}.
     */
    @Override
    public Table<T> clone() {
        return new Table<>(this);
    }


    @Override
    public String toString() {
        return rows().toString();
    }


    /**
     * Overrides each value in the table with the one returned by the given function.
     * 
     * @param initializer The initializer used to initialize the objects on the table
     */
    public void initialize(Function<GridElement<T>, T> initializer) {
        forEachLoc(val -> set(val.row(), val.column(), initializer.apply(val)));
    }


    /**
     * Returns the given element located in the specified row and column.
     * 
     * @param row The row of the element
     * @param column The column of the element
     * @return The element at the specified location
     */
    @Override
    @SuppressWarnings("unchecked")
    public T get(int row, int column) {
        return (T)elements[row][column];
    }

    /**
     * Returns the specified row of the table.
     * 
     * @param index The number of the row
     * @return A list of elements in the row
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> row(int index) {
        List<T> row = new LineList(index);
        for(int i=0; i<columnCount(); i++) row.add((T)elements[index][i]);
        return row;
    }

    /**
     * Returns the specified column of the table.
     * 
     * @param index The number of the column
     * @return A list of elements in the column
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> column(int index) {
        List<T> column = new LineList(index);
        for(int i=0; i<rowCount(); i++) column.add((T)elements[i][index]);
        return column;
    }

    /**
     * Sets the element at the given location to the specified value.
     * 
     * @param row The row of the element
     * @param column The column of the element
     * @param value The new value
     */
    @Override
    @SuppressWarnings("unchecked")
    public T set(int row, int column, T value) {
        T old = (T)elements[row][column];
        elements[row][column] = value;
        return old;
    }


    /**
     * Runs the given action for each element in the table. As argument will be passed the current object and its
     * location in the table, in form of a GridContent.
     * 
     * @param element The action to perform with all elements of the table
     * @see #Grid.GridContent
     */
    @Override
    @SuppressWarnings("unchecked")
    public void forEachLoc(Consumer<GridElement<T>> element) {
        for(int column=0; column<this.elements.length; column++) for(int row=0; row<this.elements[column].length; row++) {
            element.accept(new GridElement<>((T)this.elements[column][row], column, row));
        }
    }

    /**
     * Runs the given action for each element in the table. As argument will be passed (only) the current object.
     * 
     * @param element The action to perform with all elements of the table
     */
    @Override
    public void forEach(Consumer<? super T> element) {
        forEachLoc(content -> element.accept(content.value));
    }

    /**
     * Clears all elements of the table.
     */
    @Override
    public void clear() {
        forEachLoc(loc -> elements[loc.row()][loc.column()] = null);
    }


    /**
     * Returns all elements in the table.
     * 
     * @return All elements
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> all() {
        List<T> list = new ArrayList<>();
        for(Object[] row : elements) for(Object element : row) {
            list.add((T)element);
        }
        return list;
    }

    /**
     * Returns all elements in the table for that the given condition is fullfilled.
     * 
     * @param condition The condition to check for
     * @return The number of elements for that {@code condition} returned {@code true}
     */
    @Override
    @SuppressWarnings("unchecked")
    public List<T> all(Predicate<GridElement<T>> condition) {
        List<T> fullfilling = new ArrayList<>();
        for(int column=0; column<elements.length; column++) for(int row=0; row<elements[column].length; row++) {
            if(condition.test(new GridElement<>((T)elements[column][row], column, row))) fullfilling.add((T)elements[column][row]);
        }
        return fullfilling;
    }

    /**
     * Returns one element in the table and its location for that the given condition is fullfilled.
     * 
     * @param condition The condition to check for
     * @return An element for that the given condition is fullfilled, or {@code null}
     */
    @Override
    @SuppressWarnings("unchecked")
    public GridElement<T> oneElement(Predicate<GridElement<T>> condition) {
        for(int column=0; column<elements.length; column++) for(int row=0; row<elements[column].length; row++) {
            if(condition.test(new GridElement<>((T)elements[column][row], column, row)))
                return new GridElement<>((T)elements[column][row], column, row);
        }
        return null;
    }


    /**
     * Returns the number of rows of the table. If the table has {@code 0} columns, {@code 0}
     * will be returned.
     * 
     * @return The number of rows in this table
     */
    @Override
    public int rowCount() {
        return elements.length > 0 && elements[0].length > 0 ? elements.length : 0;
    }

    /**
     * Returns the number of columns of the table. If the table has {@code 0} rows, {@code 0}
     * will be returned.
     * 
     * @return The number of columns in this table
     */
    @Override
    public int columnCount() {
        return elements.length > 0 ? elements[0].length : 0;
    }


    /**
     * A subclass of ArrayList that takes in a  number to indentify it wehn printed out.
     */
    protected class LineList extends ArrayList<T> {

        private static final long serialVersionUID = 5768097629620867417L;
        private final int n;

        protected LineList(int n) {
            super();
            this.n = n;
        }

        @Override
        public String toString() {
            return new StringBuilder().append(n).append(':').append(super.toString()).toString();
        }
    }
}
