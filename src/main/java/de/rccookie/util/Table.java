package de.rccookie.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import de.rccookie.json.JsonArray;
import de.rccookie.json.JsonObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public interface Table<L,V> {

    List<L> columnLabels();

    List<L> rowLabels();

    default boolean columnsAreLabeled() {
        return columnLabels() != null;
    }

    default boolean rowsAreLabeled() {
        return rowLabels() != null;
    }

    @Range(from = 0)
    int indexOfColumnLabel(Object label);

    @Range(from = 0)
    int indexOfRowLabel(Object label);

    @NotNull
    ListStream<Vector<L,V>> rows();

    @NotNull
    ListStream<Vector<L,V>> columns();

    @NotNull
    Table.Vector<L,V> row(@Range(from = 0) int index);

    @NotNull
    default Table.Vector<L,V> row(Object label) {
        return row(indexOfColumnLabel(label));
    }

    @NotNull
    Table.Vector<L,V> column(@Range(from = 0) int index);

    @NotNull
    default Table.Vector<L,V> column(Object label) {
        return column(indexOfColumnLabel(label));
    }

    V value(@Range(from = 0) int row, @Range(from = 0) int column);

    default V value(int row, Object label) {
        return value(row, indexOfColumnLabel(label));
    }

    default V value(Object label, int column) {
        return value(indexOfRowLabel(label), column);
    }

    default V value(Object rowLabel, Object columnLabel) {
        return value(indexOfRowLabel(rowLabel), indexOfColumnLabel(columnLabel));
    }

    @Range(from = 0)
    int rowCount();

    @Range(from = 0)
    int columnCount();

    @Range(from = 0)
    int valueCount();

    V defaultValue();

    <U> U[][] toArray(Class<U> type);

    default Table<L,V> transpose() {
        return new ColumnMajorTable<>(this, true);
    }


    V setValue(@Range(from = 0) int row, @Range(from = 0) int column, @Nullable V value);

    default V setValue(@Range(from = 0) int row, Object label, @Nullable V value) {
        return setValue(row, indexOfColumnLabel(label), value);
    }

    default V setValue(Object label, @Range(from = 0) int column, @Nullable V value) {
        return setValue(indexOfRowLabel(label), column, value);
    }

    default V setValue(Object rowLabel, Object columnLabel, @Nullable V value) {
        return setValue(indexOfRowLabel(rowLabel), indexOfColumnLabel(columnLabel), value);
    }

    default V clearValue(@Range(from = 0) int row, @Range(from = 0) int column) {
        return setValue(row, column, null);
    }

    default V clearValue(@Range(from = 0) int row, Object label) {
        return setValue(row, label, null);
    }

    default V clearValue(Object label, @Range(from = 0) int column) {
        return setValue(label, column, null);
    }

    default V clearValue(Object rowLabel, Object columnLabel) {
        return setValue(rowLabel, columnLabel, null);
    }


    default void addRow(Collection<? extends V> values) {
        addRow(rowCount(), values);
    }

    default void addColumn(Collection<? extends V> values) {
        addColumn(rowCount(), values);
    }

    default void addRow(Map<?, ? extends V> labeledValues) {
        addRow(rowCount(), labeledValues);
    }

    default void addColumn(Map<?, ? extends V> labeledValues) {
        addColumn(rowCount(), labeledValues);
    }

    void addRow(@Range(from = 0) int index, Collection<? extends V> values);

    void addColumn(@Range(from = 0) int index, Collection<? extends V> values);

    void addRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues);

    void addColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues);


    default void addRow(L label) {
        addRow(rowCount(), label);
    }

    default void addColumn(L label) {
        addRow(columnCount(), label);
    }

    default void addRow(@Range(from = 0) int index, L label) {
        addRow(index, label, (Collection<? extends V>) null);
    }

    default void addColumn(@Range(from = 0) int index, L label) {
        addColumn(index, label, (Collection<? extends V>) null);
    }

    default void addRow(L label, @Nullable Collection<? extends V> values) {
        addRow(rowCount(), label, values);
    }

    default void addColumn(L label, @Nullable Collection<? extends V> values) {
        addColumn(rowCount(), label, values);
    }

    default void addRow(L label, @Nullable Map<?, ? extends V> labeledValues) {
        addRow(rowCount(), label, labeledValues);
    }

    default void addColumn(L label, @Nullable Map<?, ? extends V> labeledValues) {
        addColumn(rowCount(), label, labeledValues);
    }

    void addRow(@Range(from = 0) int index, L label, Collection<? extends V> values);

    void addColumn(@Range(from = 0) int index, L label, Collection<? extends V> values);

    void addRow(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues);

    void addColumn(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues);



    default void addRowsOrdered(Collection<? extends Collection<? extends V>> rows) {
        addRowsOrdered(rowCount(), rows);
    }

    default void addColumnsOrdered(Collection<? extends Collection<? extends V>> columns) {
        addColumnsOrdered(columnCount(), columns);
    }

    default void addRowsOrdered(Table<?, ? extends V> table) {
        addRowsOrdered(rowCount(), table);
    }

    default void addColumnsOrdered(Table<?, ? extends V> table) {
        addColumnsOrdered(columnCount(), table);
    }

    default void addRowsLabeled(Collection<? extends Map<?, ? extends V>> rows) {
        addRowsLabeled(rowCount(), rows);
    }

    default void addColumnsLabeled(Collection<? extends Map<?, ? extends V>> columns) {
        addColumnsLabeled(columnCount(), columns);
    }

    default void addRowsLabeled(Table<?, ? extends V> table) {
        addRowsLabeled(rowCount(), table);
    }

    default void addColumnsLabeled(Table<?, ? extends V> table) {
        addColumnsLabeled(columnCount(), table);
    }

    void addRowsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> rows);

    void addColumnsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> columns);

    void addRowsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table);

    void addColumnsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table);

    void addRowsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> rows);

    void addColumnsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> columns);

    void addRowsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table);

    void addColumnsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table);


    default void setRow(L label, Collection<? extends V> values) {
        setRow(indexOfColumnLabel(label), values);
    }

    default void setColumn(L label, Collection<? extends V> values) {
        setColumn(indexOfRowLabel(label), values);
    }

    default void setRow(L label, Map<?, ? extends V> labeledValues) {
        setRow(indexOfColumnLabel(label), labeledValues);
    }

    default void setColumn(L label, Map<?, ? extends V> labeledValues) {
        setColumn(indexOfRowLabel(label), labeledValues);
    }


    void setRow(@Range(from = 0) int index, Collection<? extends V> values);

    void setColumn(@Range(from = 0) int index, Collection<? extends V> values);

    void setRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues);

    void setColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues);


    void removeRow(@Range(from = 0) int index);

    void removeColumn(@Range(from = 0) int index);

    default void removeRow(Object label) {
        removeRow(indexOfRowLabel(label));
    }

    default void removeColumn(Object label) {
        removeColumn(indexOfColumnLabel(label));
    }


//    void renameColumn(Object oldLabel, L newLabel);
//
//    void renameRow(Object oldLabel, L newLabel);

    void setColumnLabels(Collection<? extends L> labels);

    void setRowLabels(Collection<? extends L> labels);

    @SuppressWarnings("unchecked")
    default void setColumnLabels(L... labels) {
        setColumnLabels(Arrays.asList(labels));
    }

    @SuppressWarnings("unchecked")
    default void setRowLabels(L... labels) {
        setRowLabels(Arrays.asList(labels));
    }


    void clear();


    interface Vector<L,V> extends Iterable<V> {

        L label();

        L setLabel(L label);

        @Range(from = 0)
        int index();

        @Range(from = 0)
        int size();

        V get(int index);

        V get(Object label);

        boolean contains(Object value);

        V set(int index, @Nullable V value);

        V set(Object label, @Nullable V value);

        default V clear(int index) {
            return set(index, defaultValue());
        }

        default V clear(Object label) {
            return set(label, defaultValue());
        }

        default V defaultValue() {
            return null;
        }

        void clear();

        @NotNull
        List<V> asList();

        @NotNull
        Map<?,V> asMap();
    }


//    static <L,V> Vector<L,V> vector(@Range(from = 0) int index, @Nullable L label, Collection<? extends V> values, V defaultValue, @Nullable ) {
//        Arguments.checkRange(index, 0, null);
//        List<V> data = new ArrayList<>(values);
//        for(int i=0; i<data.size(); i++)
//            if(Objects.equals(data.get(i), defaultValue))
//                data.set(i, null);
//        while(!data.isEmpty() && (data.get(data.size() - 1) == null || Objects.equals(data.get(data.size() - 1), defaultValue)))
//            data.remove(data.size() - 1);
//        return new Vector<>() {
//            @Override
//            public L label() {
//                if(label == null)
//                    throw new IllegalStateException("Vector is not labeled");
//                return label;
//            }
//
//            @Override
//            public L setLabel(L label) {
//                if(label == null)
//                    throw new IllegalStateException("Vector is not labeled");
//                throw new ImmutabilityException();
//            }
//
//            @Override
//            public @Range(from = 0) int index() {
//                return index;
//            }
//
//            @Override
//            public @Range(from = 0) int size() {
//                return size < 0 ? values.size() : size;
//            }
//
//            @Override
//            public V get(int index) {
//                Arguments.checkRange(index, 0, size < 0 ? null : size);
//                if(index >= data.size())
//                    return defaultValue;
//                V value = data.get(index);
//                return value == null ? defaultValue : value;
//            }
//
//            @Override
//            public V get(Object label) {
//                return
//            }
//
//            @Override
//            public V set(int index, @Nullable V value) {
//                return null;
//            }
//
//            @Override
//            public V set(Object label, @Nullable V value) {
//                return null;
//            }
//
//            @Override
//            public void clear() {
//
//            }
//
//            @Override
//            public @NotNull List<V> asList() {
//                return null;
//            }
//
//            @Override
//            public @NotNull Map<?, V> asMap() {
//                return null;
//            }
//
//            @NotNull
//            @Override
//            public Iterator<V> iterator() {
//                return null;
//            }
//        };
//    }

    static Object toKeyedJson(Table<? extends String, ?> table) {
        boolean columnsAreLabeled = table.columnsAreLabeled();
        if(table.rowsAreLabeled()) {
            JsonObject json = new JsonObject();
            for(Vector<? extends String, ?> row : table.rows())
                json.put(row.label(), columnsAreLabeled ? row.asMap() : row.asList());
            return json;
        }
        if(columnsAreLabeled) {
            JsonArray json = new JsonArray();
            for(Vector<? extends String, ?> row : table.rows())
                json.add(row.asMap());
            return json;
        }
        return toArrayJson(table);
    }

    static Object toArrayJson(Table<?,?> table) {
        return table.toArray(Object.class);
    }
}
