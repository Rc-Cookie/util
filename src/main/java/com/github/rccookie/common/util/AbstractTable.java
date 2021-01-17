package com.github.rccookie.common.util;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents a grid with a finite number on rows and columns.
 */
public interface AbstractTable<T> extends Grid<T> {

    /**
     * Returns the number of rows that the table contains. If the table
     * has {@code 0} columns, {@code 0} will be returned.
     * 
     * @return The number of rows in this table
     */
    public int rowCount();
    
    /**
     * Returns the number of columns that the table contains. If the table
     * has {@code 0} rows, {@code 0} will be returned.
     * 
     * @return The number of columns in this table
     */
    public int columnCount();


    /**
     * Returns a list of lists of elements in each row.
     * 
     * @return All elements ordered in rows
     */
    public default List<List<T>> rows() {
        List<List<T>> rows = new ArrayList<>();
        for(int i=0; i<rowCount(); i++) rows.add(row(i));
        return rows;
    }
    
    /**
     * Returns a list of lists of elements in each column.
     * 
     * @return All elements ordered in columns
     */
    public default List<List<T>> columns() {
        List<List<T>> columns = new ArrayList<>();
        for(int i=0; i<columnCount(); i++) columns.add(column(i));
        return columns;
    }



    /**
     * Returns a copy of this table. Weather the actual elements in the table
     * are cloned too may differ between implementations.
     * 
     * @return A clone of this table
     */
    public AbstractTable<T> clone();
}
