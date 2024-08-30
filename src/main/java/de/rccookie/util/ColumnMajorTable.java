package de.rccookie.util;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.rccookie.util.text.Alignment;
import de.rccookie.util.text.TableRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class ColumnMajorTable<L,V> implements Table<L,V> {

    private final Table<L,V> table;


    public ColumnMajorTable() {
        this((V) null);
    }

    public ColumnMajorTable(V defaultValue) {
        this(new RowMajorTable<>("Column", "Row", defaultValue), true);
    }

    public ColumnMajorTable(@Nullable Collection<? extends L> columnLabels) {
        this(null, columnLabels);
    }

    public ColumnMajorTable(V defaultValue, @Nullable Collection<? extends L> columnLabels) {
        this(new RowMajorTable<>("Column", "Row", defaultValue, columnLabels), true);
    }

    @SafeVarargs
    public ColumnMajorTable(L... columnLabels) {
        this(null, columnLabels);
    }

    @SafeVarargs
    public ColumnMajorTable(V defaultValue, L... columnLabels) {
        this(Arrays.asList(columnLabels));
    }

    public ColumnMajorTable(Table<? extends L, ? extends V> table) {
        this(new RowMajorTable<>("Row", "Column", table), true);
    }

    ColumnMajorTable(Table<L,V> table, boolean ignored) {
        this.table = Arguments.checkNull(table, "table");
    }


    @Override
    public String toString() {
        return new TableRenderer(this)
                .alignment(Alignment.LEFT, Alignment.TOP)
                .toString();
    }


    @Override
    public List<L> columnLabels() {
        return table.rowLabels();
    }

    @Override
    public List<L> rowLabels() {
        return table.columnLabels();
    }

    @Override
    public @Range(from = 0) int indexOfColumnLabel(Object label) {
        return table.indexOfRowLabel(label);
    }

    @Override
    public @Range(from = 0) int indexOfRowLabel(Object label) {
        return table.indexOfColumnLabel(label);
    }

    @Override
    public @NotNull ListStream<Vector<L,V>> rows() {
        return table.columns();
    }

    @Override
    public @NotNull ListStream<Vector<L,V>> columns() {
        return table.rows();
    }

    @Override
    public @NotNull Vector<L,V> row(@Range(from = 0) int index) {
        return table.column(index);
    }

    @Override
    public @NotNull Vector<L,V> column(@Range(from = 0) int index) {
        return table.row(index);
    }

    @Override
    public V value(@Range(from = 0) int row, @Range(from = 0) int column) {
        return table.value(column, row);
    }

    @Override
    public @Range(from = 0) int rowCount() {
        return table.columnCount();
    }

    @Override
    public @Range(from = 0) int columnCount() {
        return table.rowCount();
    }

    @Override
    public @Range(from = 0) int valueCount() {
        return table.valueCount();
    }

    @Override
    public V defaultValue() {
        return table.defaultValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U[][] toArray(Class<U> type) {
        U[][] arr = (U[][]) Array.newInstance(type, rowCount(), columnCount());
        for(int i=0; i<arr.length; i++) {
            Vector<L,V> row = table.row(i);
            for(int j=0; j<row.size(); j++)
                arr[i][j] = (U) row.get(j);
            Arrays.fill(arr[i], row.size(), arr[i].length, defaultValue());
        }
        return arr;
    }

    @Override
    public Table<L, V> transpose() {
        return table;
    }

    @Override
    public V setValue(@Range(from = 0) int row, @Range(from = 0) int column, @Nullable V value) {
        return table.setValue(column, row, value);
    }

    @Override
    public void addRow(@Range(from = 0) int index, Collection<? extends V> values) {
        table.addColumn(index, values);
    }

    @Override
    public void addColumn(@Range(from = 0) int index, Collection<? extends V> values) {
        table.addRow(index, values);
    }

    @Override
    public void addRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        table.addColumn(index, labeledValues);
    }

    @Override
    public void addColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        table.addRow(index, labeledValues);
    }

    @Override
    public void addRow(@Range(from = 0) int index, L label, Collection<? extends V> values) {
        table.addColumn(index, label, values);
    }

    @Override
    public void addColumn(@Range(from = 0) int index, L label, Collection<? extends V> values) {
        table.addRow(index, label, values);
    }

    @Override
    public void addRow(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues) {
        table.addColumn(index, label, labeledValues);
    }

    @Override
    public void addColumn(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues) {
        table.addRow(index, label, labeledValues);
    }

    @Override
    public void addRowsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> rows) {
        table.addColumnsOrdered(index, rows);
    }

    @Override
    public void addColumnsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> columns) {
        table.addRowsOrdered(index, columns);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addRowsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table) {
        table.addColumnsOrdered(index, (Table) table);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addColumnsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table) {
        table.addRowsOrdered(index, (Table) table);
    }

    @Override
    public void addRowsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> rows) {
        table.addColumnsLabeled(index, rows);
    }

    @Override
    public void addColumnsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> columns) {
        table.addRowsLabeled(index, columns);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addRowsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table) {
        table.addColumnsLabeled(index, (Table) table);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Override
    public void addColumnsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table) {
        table.addRowsLabeled(index, (Table) table);
    }

    @Override
    public void setRow(@Range(from = 0) int index, Collection<? extends V> values) {
        table.setColumn(index, values);
    }

    @Override
    public void setColumn(@Range(from = 0) int index, Collection<? extends V> values) {
        table.setRow(index, values);
    }

    @Override
    public void setRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        table.setColumn(index, labeledValues);
    }

    @Override
    public void setColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        table.setRow(index, labeledValues);
    }

    @Override
    public void removeRow(@Range(from = 0) int index) {
        table.removeColumn(index);
    }

    @Override
    public void removeColumn(@Range(from = 0) int index) {
        table.removeRow(index);
    }

    @Override
    public void setColumnLabels(Collection<? extends L> labels) {
        table.setRowLabels(labels);
    }

    @Override
    public void setRowLabels(Collection<? extends L> labels) {
        table.setColumnLabels(labels);
    }

    @Override
    public void clear() {
        table.clear();
    }
}
