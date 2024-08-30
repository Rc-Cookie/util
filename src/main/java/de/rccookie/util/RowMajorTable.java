package de.rccookie.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.rccookie.math.Mathf;
import de.rccookie.util.text.Alignment;
import de.rccookie.util.text.TableRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public class RowMajorTable<L,V> implements Table<L,V> {

//    static {
//        Json.registerDeserializer(RowMajorTable.class, json -> {
//            if(json.isObject() && json.containsKey("data")) {
//                RowMajorTable table = new RowMajorTable(
//            }
//        });
//    }

    private final String rowStr;
    private final String columnStr;
    private List<L> rowLabels;
    private List<L> columnLabels;
    private Map<L, Integer> rowLabelsToIndex;
    private Map<L, Integer> columnLabelsToIndex;
    private final List<List<V>> rows = new ArrayList<>();
    private final V defaultValue;



    public RowMajorTable() {
        this((V) null);
    }

    public RowMajorTable(V defaultValue) {
        this("Row", "Column", defaultValue);
    }

    RowMajorTable(String rowStr, String columnStr, V defaultValue) {
        this.rowStr = rowStr;
        this.columnStr = columnStr;
        this.defaultValue = defaultValue;
    }

    public RowMajorTable(@Nullable Collection<? extends L> columnLabels) {
        this(null, columnLabels);
    }

    public RowMajorTable(V defaultValue, @Nullable Collection<? extends L> columnLabels) {
        this("Row", "Column", defaultValue, columnLabels);
    }

    RowMajorTable(String rowStr, String columnStr, V defaultValue, @Nullable Collection<? extends L> columnLabels) {
        this(rowStr, columnStr, defaultValue);
        setColumnLabels(columnLabels);
    }

    @SafeVarargs
    public RowMajorTable(L... columnLabels) {
        this(null, columnLabels);
    }

    @SafeVarargs
    public RowMajorTable(V defaultValue, L... columnLabels) {
        this(Arrays.asList(columnLabels));
    }

    public RowMajorTable(Table<? extends L, ? extends V> table) {
        this("Row", "Column", table);
    }

    RowMajorTable(String rowStr, String columnStr, Table<? extends L, ? extends V> table) {
        this(rowStr, columnStr, table.defaultValue());
        setColumnLabels(table.columnLabels());
        setRowLabels(table.rowLabels());
        int i = 0;
        for(Vector<?, ? extends V> row : table.rows())
            setRow(i++, row.asList());
    }


    @Override
    public String toString() {
        return new TableRenderer(this)
                .alignment(Alignment.LEFT, Alignment.TOP)
                .toString();
    }

    @Override
    public List<L> columnLabels() {
        if(columnLabelsToIndex == null) return null;
        return Utils.view(columnLabels);
    }

    @Override
    public List<L> rowLabels() {
        if(rowLabelsToIndex == null) return null;
        return Utils.view(rowLabels);
    }

    @Override
    public @Range(from = 0) int indexOfColumnLabel(Object label) {
        if(columnLabelsToIndex == null)
            throw new IllegalStateException(columnStr+"s of the table are not labeled");
        //noinspection SuspiciousMethodCalls
        Integer index = columnLabelsToIndex.get(label);
        if(index == null)
            throw new IllegalArgumentException(columnStr+"Column label '"+label+"' does not exist");
        return index;
    }

    @Override
    public @Range(from = 0) int indexOfRowLabel(Object label) {
        if(rowLabelsToIndex == null)
            throw new IllegalStateException(rowStr+"Rows of the table are not labeled");
        //noinspection SuspiciousMethodCalls
        Integer index = rowLabelsToIndex.get(label);
        if(index == null)
            throw new IllegalArgumentException(rowStr+"Row label '"+label+"' does not exist");
        return index;
    }

    @Override
    public @NotNull ListStream<Vector<L,V>> rows() {
        return ListStream.iterate(0, i -> i+1).limit(rowCount()).map(Row::new);
    }

    @Override
    public @NotNull ListStream<Vector<L, V>> columns() {
        return ListStream.iterate(0, i -> i+1).limit(columnCount()).map(Column::new);
    }

    @Override
    public @NotNull Vector<L,V> row(@Range(from = 0) int index) {
        return new Row(Arguments.checkRange(index, 0, rowCount()));
    }

    @Override
    public @NotNull Vector<L, V> column(@Range(from = 0) int index) {
        return new Column(Arguments.checkRange(index, 0, columnCount()));
    }

    @Override
    public V value(@Range(from = 0) int row, @Range(from = 0) int column) {
        Arguments.checkRange(row, 0, rowLabels != null ? rowLabels.size() : null);
        Arguments.checkRange(column, 0, columnLabels != null ? columnLabels.size() : null);
        if(row >= rows.size() || column >= rows.get(row).size())
            return defaultValue;
        V value = rows.get(row).get(column);
        return value != null ? value : defaultValue;
    }

    @Override
    public @Range(from = 0) int rowCount() {
        return rowLabels != null ? rowLabels.size() : rows.size();
    }

    @Override
    public @Range(from = 0) int columnCount() {
        return columnLabels != null ? columnLabels.size() : Mathf.max(rows, List::size);
    }

    @Override
    public @Range(from = 0) int valueCount() {
        if(columnLabels != null || rowLabels != null)
            return columnCount() * rowCount();
        return Mathf.sum(rows, List::size);
    }

    @Override
    public V defaultValue() {
        return defaultValue;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U[][] toArray(Class<U> type) {
        U[][] arr = (U[][]) Array.newInstance(type, rowCount(), columnCount());
        for(int i=0; i<arr.length; i++) {
            List<V> row = rows.get(i);
            for(int j=0; j<row.size(); j++)
                arr[i][j] = (U) row.get(j);
            Arrays.fill(arr[i], row.size(), arr[i].length, defaultValue);
        }
        return arr;
    }

//    @Override
//    public Object toJson() {
//        return new JsonObject(
//                "defaultValue", defaultValue,
//                "rowLabels", rowLabels,
//                "columnLabels", columnLabels,
//                "transposed", rowStr.equals("Column"),
//                "data", rows
//        );
//    }

    private void trim() {
        for(int i=rows.size()-1; i>=0; i--) {
            if(!rows.get(i).isEmpty()) return;
            rows.remove(i);
        }
    }

    private void trim(List<V> row) {
        for(int i=row.size()-1; i>=0; i--) {
            if(row.get(i) != null) return;
            row.remove(i);
        }
    }

    private void trim(int row) {
        trim(rows.get(row));
        if(row == rows.size() - 1)
            trim();
    }

    @Override
    public V setValue(@Range(from = 0) int row, @Range(from = 0) int column, @Nullable V value) {
        Arguments.checkRange(row, 0, rowLabels != null ? rowLabels.size() : null);
        Arguments.checkRange(column, 0, columnLabels != null ? columnLabels.size() : null);
        if(value == null || value.equals(defaultValue)) {
            if(row >= rows.size() || column >= rows.get(row).size())
                return defaultValue;
            List<V> r = rows.get(row);
            V prev = r.set(column, null);
            trim(row);
            return prev;
        }

        while(rows.size() <= row)
            rows.add(new ArrayList<>());
        List<V> r = rows.get(row);
        while(r.size() <= column)
            r.add(null);
        return r.set(column, value);
    }

    private void addRow0(int index, @Nullable L label, List<V> row) {
        if((label == null) != (rowLabels == null))
            throw new IllegalStateException(rowStr+"Row are "+(rowLabels!=null?"labeled":"unlabeled")+", cannot add "+(label!=null?"labeled":"unlabeled")+" "+rowStr.toLowerCase());
        if(columnLabels != null && row.size() > columnLabels.size())
            throw new IllegalArgumentException("Too many values ("+row.size()+") given for table with "+columnCount()+" "+columnStr.toLowerCase()+"s");
        Arguments.checkRange(index, 0, label != null ? rowLabels.size() + 1 : null);

        if(label != null) {
            if(rowLabelsToIndex.containsKey(label))
                throw new IllegalArgumentException(row+" label '"+label+"' does already exist");
            rowLabels.add(index, label);
            for(int i=index; i<rowLabels.size(); i++)
                rowLabelsToIndex.put(rowLabels.get(i), i);
        }

        for(int i=0; i<row.size(); i++)
            if(Objects.equals(row.get(i), defaultValue))
                row.set(i, null);
        trim(row);
        if(!row.isEmpty() || index < rows.size()) {
            while(index > rows.size())
                rows.add(new ArrayList<>());
            rows.add(index, row);
        }
    }

    private void addColumn0(int index, @Nullable L label, Collection<? extends V> values) {
        if((label == null) != (columnLabels == null))
            throw new IllegalStateException(columnStr+"s are "+(columnLabels!=null?"labeled":"unlabeled")+", cannot add "+(label!=null?"labeled":"unlabeled")+" "+columnStr.toLowerCase());
        if(rowLabels != null && values.size() > rowLabels.size())
            throw new IllegalArgumentException("Too many values ("+values.size()+") given for table with "+columnCount()+" "+rowStr.toLowerCase()+"s");
        Arguments.checkRange(index, 0, label != null ? columnLabels.size() + 1 : null);

        if(label != null) {
            if(columnLabelsToIndex.containsKey(label))
                throw new IllegalArgumentException(columnStr+" label '"+label+"' does already exist");
            columnLabels.add(index, label);
            for(int i=index; i<columnLabels.size(); i++)
                columnLabelsToIndex.put(columnLabels.get(i), i);
        }

        int i = 0;
        for(V v : values) {
            if(Objects.equals(v, defaultValue))
                v = null;
            if(i == rows.size())
                rows.add(new ArrayList<>());

            List<V> row = rows.get(i++);
            if(v != null || index < row.size()) {
                while(index > row.size())
                    row.add(null);
                row.add(index, v);
            }
        }
        trim();
    }

    private List<V> labeledValuesToRow(Map<?, ? extends V> labeledValues) {
        if(columnLabels == null)
            throw new IllegalStateException(columnStr+"s are not labeled, cannot add values by label");

        List<V> row = new ArrayList<>(columnLabels.size());
        for(L label : columnLabels)
            row.add(labeledValues.get(label));
        return row;
    }

    private List<V> labeledValuesToColumn(Map<?, ? extends V> labeledValues) {
        if(rowLabels == null)
            throw new IllegalStateException(rowStr+"s are not labeled, cannot add values by label");

        List<V> column = new ArrayList<>(rowLabels.size());
        for(L label : rowLabels)
            column.add(labeledValues.get(label));
        return column;
    }

    @Override
    public void addRow(@Range(from = 0) int index, Collection<? extends V> values) {
        addRow0(index, null, new ArrayList<>(values));
    }

    @Override
    public void addColumn(@Range(from = 0) int index, Collection<? extends V> values) {
        addColumn0(index, null, new ArrayList<>(values));
    }

    @Override
    public void addRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        addRow0(index, null, labeledValuesToRow(labeledValues));
    }

    @Override
    public void addColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        addColumn0(index, null, labeledValuesToColumn(labeledValues));
    }

    @Override
    public void addRow(@Range(from = 0) int index, L label, Collection<? extends V> values) {
        addRow0(index, Arguments.checkNull(label, "label"), new ArrayList<>(values));
    }

    @Override
    public void addColumn(@Range(from = 0) int index, L label, Collection<? extends V> values) {
        addColumn0(index, Arguments.checkNull(label, "label"), new ArrayList<>(values));
    }

    @Override
    public void addRow(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues) {
        addRow0(index, Arguments.checkNull(label, "label"), labeledValuesToRow(labeledValues));
    }

    @Override
    public void addColumn(@Range(from = 0) int index, L label, Map<?, ? extends V> labeledValues) {
        addColumn0(index, Arguments.checkNull(label, "label"), labeledValuesToColumn(labeledValues));
    }

    @Override
    public void addRowsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> rows) {
        for(Collection<? extends V> row : rows)
            addRow(index++, row);
    }

    @Override
    public void addColumnsOrdered(@Range(from = 0) int index, Collection<? extends Collection<? extends V>> columns) {
        for(Collection<? extends V> column : columns)
            addColumn(index++, column);
    }

    @Override
    public void addRowsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table) {
        for(Vector<?, ? extends V> row : table.rows())
            addRow(index++, row.asList());
    }

    @Override
    public void addColumnsOrdered(@Range(from = 0) int index, Table<?, ? extends V> table) {
        for(Vector<?, ? extends V> column : table.columns())
            addRow(index++, column.asList());
    }

    @Override
    public void addRowsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> rows) {
        for(Map<?, ? extends V> row : rows)
            addRow(index++, row);
    }

    @Override
    public void addColumnsLabeled(@Range(from = 0) int index, Collection<? extends Map<?, ? extends V>> columns) {
        for(Map<?, ? extends V> column : columns)
            addColumn(index++, column);
    }

    @Override
    public void addRowsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table) {
        for(Vector<?, ? extends V> row : table.rows())
            addRow(index++, row.asMap());
    }

    @Override
    public void addColumnsLabeled(@Range(from = 0) int index, Table<?, ? extends V> table) {
        for(Vector<?, ? extends V> column : table.columns())
            addRow(index++, column.asMap());
    }

    @Override
    public void setRow(@Range(from = 0) int index, Collection<? extends V> values) {
        Arguments.checkNull(values, "values");
        Arguments.checkRange(index, 0, rowLabels != null ? rowLabels.size() : null);
        if(columnLabels != null && values.size() > columnLabels.size())
            throw new IllegalArgumentException("Too many values ("+values.size()+") given for table with "+columnCount()+" "+columnStr.toLowerCase()+"s");

        while(rows.size() <= index)
            rows.add(new ArrayList<>());
        List<V> row = rows.get(index);
        row.clear();
        for(V v : values)
            row.add(Objects.equals(v, defaultValue) ? null : v);
        trim(index);
    }

    @Override
    public void setColumn(@Range(from = 0) int index, Collection<? extends V> values) {
        Arguments.checkNull(values, "values");
        Arguments.checkRange(index, 0, columnLabels != null ? columnLabels.size() : null);
        if(rowLabels != null && values.size() > rowLabels.size())
            throw new IllegalArgumentException("Too many values ("+values.size()+") given for table with "+columnCount()+" "+rowStr.toLowerCase()+"s");

        int i = 0;
        for(V v : values) {
            if(Objects.equals(v, defaultValue))
                v = null;

            List<V> row = rows.get(i++);
            if(v == null) {
                if(index >= row.size()) continue;
                row.set(index, null);
                trim(row);
            }
            else {
                while(index >= row.size())
                    row.add(null);
                row.set(index, v);
            }
        }
        trim();
    }

    @Override
    public void setRow(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        setRow(index, labeledValuesToRow(labeledValues));
    }

    @Override
    public void setColumn(@Range(from = 0) int index, Map<?, ? extends V> labeledValues) {
        setColumn(index, labeledValuesToColumn(labeledValues));
    }

    @Override
    public void removeRow(@Range(from = 0) int index) {
        removeLabel(index, rowLabels, rowLabelsToIndex);

        if(index < rows.size()) {
            rows.remove(index);
            trim();
        }
    }

    @Override
    public void removeColumn(@Range(from = 0) int index) {
        removeLabel(index, columnLabels, columnLabelsToIndex);

        for(List<V> row : rows) {
            if(index < row.size()) {
                row.remove(index);
                trim(row);
            }
        }
        trim();
    }

    private void removeLabel(@Range(from = 0) int index, List<L> labels, Map<L, Integer> labelsToIndex) {
        Arguments.checkRange(index, 0, labels != null ? labels.size() : null);

        if(labels != null) {
            labelsToIndex.remove(labels.remove(index));
            for(int i = index; i < labels.size(); i++)
                labelsToIndex.put(labels.get(i), i);
        }
    }

    @Override
    public void setColumnLabels(Collection<? extends L> labels) {
        if(Objects.equals(labels, columnLabels)) return;

        if(labels != null) {
            if(columnLabels == null || labels.size() < columnLabels.size())
                for(List<V> row : rows)
                    while(row.size() > labels.size())
                        row.remove(row.size() - 1);

            columnLabels = new ArrayList<>(labels);
            columnLabelsToIndex = new HashMap<>();
            for(int i = 0; i < columnLabels.size(); i++)
                columnLabelsToIndex.put(columnLabels.get(i), i);
        }
        else {
            columnLabels = null;
            columnLabelsToIndex = null;
        }
    }

    @Override
    public void setRowLabels(Collection<? extends L> labels) {
        if(Objects.equals(labels, rowLabels)) return;

        if(labels != null) {
            if(rowLabels == null || labels.size() < rowLabels.size())
                while(rows.size() > labels.size())
                    rows.remove(rows.size() - 1);

            rowLabels = new ArrayList<>(labels);
            rowLabelsToIndex = new HashMap<>();
            for(int i = 0; i < rowLabels.size(); i++)
                rowLabelsToIndex.put(rowLabels.get(i), i);
        }
        else {
            rowLabels = null;
            rowLabelsToIndex = null;
        }
    }

    @Override
    public void clear() {
        rows.clear();
    }



    private abstract class AbstractVector implements Table.Vector<L,V> {

        final int index;

        AbstractVector(int index) {
            this.index = index;
        }

        abstract String type();

        abstract List<L> labels();

        abstract Map<L, Integer> labelsToIndex();

        @Override
        public String toString() {
            //noinspection UnnecessaryUnicodeEscape
            return (labels() != null ? label()+" \u2551 " : "") + asList().stream().map(Objects::toString).collect(Collectors.joining(" | "));
        }

        @Override
        public L label() {
            if(labels() == null)
                throw new IllegalStateException(type()+"s of the table are not labeled");
            return labels().get(index);
        }

        @Override
        public L setLabel(L label) {
            if(labels() == null)
                throw new IllegalStateException(type()+"s of the table are not labeled");
            if(labelsToIndex().containsKey(label))
                throw new IllegalArgumentException(type()+" label '"+label+"' does already exist");
            L old = rowLabels.set(index, label);
            labelsToIndex().remove(old);
            labelsToIndex().put(label, index);
            return old;
        }

        @Override
        public @Range(from = 0) int index() {
            return index;
        }

        @NotNull
        @Override
        public Iterator<V> iterator() {
            return Stream.iterate(0, i->i+1).limit(size()).map(i -> get((int) i)).iterator();
        }
    }

    private final class Row extends AbstractVector {

        private Row(int index) {
            super(index);
        }

        @Override
        String type() {
            return rowStr;
        }

        @Override
        List<L> labels() {
            return rowLabels;
        }

        @Override
        Map<L, Integer> labelsToIndex() {
            return rowLabelsToIndex;
        }

        @Override
        public @Range(from = 0) int size() {
            return columnLabels != null ? columnLabels.size() : index < rows.size() ? rows.get(index).size() : 0;
        }

        @Override
        public V get(int index) {
            Arguments.checkRange(index, 0, columnLabels != null ? columnLabels.size() : null);
            if(this.index >= rows.size() || index >= rows.get(this.index).size())
                return defaultValue;
            V value = rows.get(this.index).get(index);
            return value != null ? value : defaultValue;
        }

        @Override
        public V get(Object label) {
            return get(indexOfColumnLabel(label));
        }

        @Override
        public boolean contains(Object value) {
            if(index >= rows.size())
                return !columnLabels.isEmpty() && (value == null || value.equals(defaultValue));
            if((value == null || value.equals(defaultValue)) && rows.get(index).size() < columnLabels.size())
                return true;
            //noinspection SuspiciousMethodCalls
            return rows.get(index).contains(value);
        }

        @Override
        public V set(int index, @Nullable V value) {
            return setValue(this.index, index, value);
        }

        @Override
        public V set(Object label, @Nullable V value) {
            return setValue(index, label, value);
        }

        @Override
        public void clear() {
            if(index >= rows.size()) return;
            rows.get(index).clear();
            trim(index);
        }

        @Override
        public @NotNull List<V> asList() {
            return new AbstractImmutableList<>() {
                @Override
                public V get(int index) {
                    return Row.this.get(index);
                }

                @Override
                public int indexOf(Object o) {
                    if(Objects.equals(o, defaultValue))
                        o = null;

                    if(index >= rows.size())
                        return columnLabels != null && !columnLabels.isEmpty() && o == null ? 0 : -1;
                    int idx = rows.get(index).indexOf(o);
                    if(idx == -1 && rows.get(index).size() < columnLabels.size() && o == null)
                        return rows.get(index).size();
                    return idx;
                }

                @Override
                public int lastIndexOf(Object o) {
                    if(Objects.equals(o, defaultValue))
                        o = null;

                    if(index >= rows.size())
                        return o == null ? columnLabels.size() - 1 : -1;
                    if(rows.get(index).size() < columnLabels.size() && o == null)
                        return columnLabels.size() - 1;
                    return rows.get(index).lastIndexOf(o);
                }

                @NotNull
                @Override
                public ListIterator<V> listIterator() {
                    return new RandomAccessListIterator<>(this);
                }

                @NotNull
                @Override
                public ListIterator<V> listIterator(int index) {
                    return new RandomAccessListIterator<>(this, index);
                }

                @NotNull
                @Override
                public List<V> subList(int fromIndex, int toIndex) {
                    return RandomAccessSubList.ofRange(this, fromIndex, toIndex);
                }

                @Override
                public int size() {
                    return Row.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return size() == 0;
                }

                @Override
                public boolean contains(Object o) {
                    return Row.this.contains(o);
                }

                @NotNull
                @Override
                public Iterator<V> iterator() {
                    return Row.this.iterator();
                }

                @SuppressWarnings("unchecked")
                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    if(a.length < size())
                        a = Arrays.copyOf(a, size());
                    for(int i=0; i<size(); i++)
                        a[i] = (T) Row.this.get(i);
                    return a;
                }

                @Override
                public boolean containsAll(@NotNull Collection<?> c) {
                    for(Object o : c)
                        if(!contains(o))
                            return false;
                    return true;
                }
            };
        }

        @Override
        public @NotNull Map<?,V> asMap() {
            if(columnLabels == null)
                throw new IllegalStateException(columnStr+"s of the table are not labeled");
            return new AbstractImmutableMap<>() {
                @Override
                public int size() {
                    return columnLabels.size();
                }

                @Override
                public boolean isEmpty() {
                    return columnLabels.isEmpty();
                }

                @Override
                public boolean containsKey(Object key) {
                    return columnLabelsToIndex.containsKey(key);
                }

                @Override
                public boolean containsValue(Object value) {
                    return Row.this.contains(value);
                }

                @Override
                public V get(Object key) {
                    return Row.this.get(key);
                }

                @NotNull
                @Override
                public Set<Object> keySet() {
                    return Utils.view(columnLabelsToIndex.keySet());
                }

                @NotNull
                @Override
                public Collection<V> values() {
                    return asList();
                }

                @NotNull
                @Override
                public Set<Map.Entry<Object,V>> entrySet() {
                    return new AbstractImmutableSet<>() {
                        @Override
                        public int size() {
                            return columnLabels.size();
                        }

                        @Override
                        public boolean isEmpty() {
                            return columnLabels.isEmpty();
                        }

                        @Override
                        public boolean contains(Object o) {
                            if(!(o instanceof Map.Entry<?,?>))
                                return false;
                            Map.Entry<?,?> e = (Entry<?,?>) o;
                            //noinspection SuspiciousMethodCalls
                            return columnLabelsToIndex.containsKey(e.getKey()) && Row.this.get(e.getKey()).equals(e.getValue());
                        }

                        @NotNull
                        @Override
                        public Iterator<Entry<Object,V>> iterator() {
                            return columnLabelsToIndex.keySet().stream().map(k -> Map.entry((Object) k, Row.this.get(k))).iterator();
                        }

                        @SuppressWarnings("unchecked")
                        @NotNull
                        @Override
                        public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                            if(a.length < size())
                                a = Arrays.copyOf(a, size());
                            int i = 0;
                            for(Object key : columnLabelsToIndex.keySet())
                                a[i] = (T) Map.entry(key, Row.this.get(key));
                            return a;
                        }

                        @Override
                        public boolean containsAll(@NotNull Collection<?> c) {
                            for(Object o : c)
                                if(!contains(o))
                                    return false;
                            return true;
                        }
                    };
                }
            };
        }
    }


    private final class Column extends AbstractVector {

        private Column(int index) {
            super(index);
        }

        @Override
        String type() {
            return columnStr;
        }

        @Override
        List<L> labels() {
            return columnLabels;
        }

        @Override
        Map<L, Integer> labelsToIndex() {
            return columnLabelsToIndex;
        }

        @Override
        public @Range(from = 0) int size() {
            return rowLabels != null ? rowLabels.size() : rows.size();
        }

        @Override
        public V get(int index) {
            Arguments.checkRange(index, 0, rowLabels != null ? rowLabels.size() : null);
            if(index >= rows.size() || this.index >= rows.get(index).size())
                return defaultValue;
            V value = rows.get(index).get(this.index);
            return value != null ? value : defaultValue;
        }

        @Override
        public V get(Object label) {
            return get(indexOfRowLabel(label));
        }

        @Override
        public boolean contains(Object value) {
            for(List<V> row : rows) {
                if(index >= row.size()) {
                    if(value == null || value.equals(defaultValue))
                        return true;
                }
                else if(Objects.equals(value, row.get(index)))
                    return true;
            }
            return false;
        }

        @Override
        public V set(int index, @Nullable V value) {
            return setValue(index, this.index, value);
        }

        @Override
        public V set(Object label, @Nullable V value) {
            return setValue(label, this.index, value);
        }

        @Override
        public void clear() {
            for(List<V> row : rows) {
                if(row.size() <= index) continue;
                row.set(index, null);
                trim(row);
            }
            trim();
        }

        @Override
        public @NotNull List<V> asList() {
            return new AbstractImmutableList<>() {
                @Override
                public V get(int index) {
                    return Column.this.get(index);
                }

                @Override
                public int indexOf(Object o) {
                    if(Objects.equals(o, defaultValue))
                        o = null;

                    for(int i=0; i<rows.size(); i++) {
                        if(rows.get(i).size() <= index) {
                            if(o == null) return i;
                        }
                        else {
                            if(Objects.equals(o, rows.get(i).get(index)))
                                return i;
                        }
                    }

                    return rowLabels != null && rowLabels.size() > rows.size() ? rows.size() : -1;
                }

                @Override
                public int lastIndexOf(Object o) {
                    if(Objects.equals(o, defaultValue))
                        o = null;

                    if(rowLabels != null && rowLabels.size() > rows.size() && o == null)
                        return rows.size() - 1;

                    for(int i=rows.size()-1; i>=0; i--) {
                        if(rows.get(i).size() <= index) {
                            if(o == null) return i;
                        }
                        else {
                            if(Objects.equals(o, rows.get(i).get(index)))
                                return i;
                        }
                    }
                    return -1;
                }

                @NotNull
                @Override
                public ListIterator<V> listIterator() {
                    return new RandomAccessListIterator<>(this);
                }

                @NotNull
                @Override
                public ListIterator<V> listIterator(int index) {
                    return new RandomAccessListIterator<>(this, index);
                }

                @NotNull
                @Override
                public List<V> subList(int fromIndex, int toIndex) {
                    return RandomAccessSubList.ofRange(this, fromIndex, toIndex);
                }

                @Override
                public int size() {
                    return Column.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return size() == 0;
                }

                @Override
                public boolean contains(Object o) {
                    return indexOf(o) != -1;
                }

                @NotNull
                @Override
                public Iterator<V> iterator() {
                    return Column.this.iterator();
                }

                @SuppressWarnings("unchecked")
                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    if(a.length < size())
                        a = Arrays.copyOf(a, size());
                    for(int i=0; i<size(); i++)
                        a[i] = (T) Column.this.get(i);
                    return a;
                }

                @Override
                public boolean containsAll(@NotNull Collection<?> c) {
                    for(Object o : c)
                        if(!contains(o))
                            return false;
                    return true;
                }
            };
        }

        @Override
        public @NotNull Map<?, V> asMap() {
            if(rowLabels == null)
                throw new IllegalStateException(rowStr+"s of the table are not labeled");
            return new AbstractImmutableMap<>() {
                @Override
                public int size() {
                    return rowLabels.size();
                }

                @Override
                public boolean isEmpty() {
                    return rowLabels.isEmpty();
                }

                @Override
                public boolean containsKey(Object key) {
                    return rowLabelsToIndex.containsKey(key);
                }

                @Override
                public boolean containsValue(Object value) {
                    return Column.this.contains(value);
                }

                @Override
                public V get(Object key) {
                    return Column.this.get(key);
                }

                @NotNull
                @Override
                public Set<Object> keySet() {
                    return Utils.view(rowLabelsToIndex.keySet());
                }

                @NotNull
                @Override
                public Collection<V> values() {
                    return asList();
                }

                @NotNull
                @Override
                public Set<Map.Entry<Object,V>> entrySet() {
                    return new AbstractImmutableSet<>() {
                        @Override
                        public int size() {
                            return rowLabels.size();
                        }

                        @Override
                        public boolean isEmpty() {
                            return rowLabels.isEmpty();
                        }

                        @Override
                        public boolean contains(Object o) {
                            if(!(o instanceof Map.Entry<?,?>))
                                return false;
                            Map.Entry<?,?> e = (Entry<?,?>) o;
                            //noinspection SuspiciousMethodCalls
                            return rowLabelsToIndex.containsKey(e.getKey()) && Column.this.get(e.getKey()).equals(e.getValue());
                        }

                        @NotNull
                        @Override
                        public Iterator<Entry<Object,V>> iterator() {
                            return rowLabelsToIndex.keySet().stream().map(k -> Map.entry((Object) k, Column.this.get(k))).iterator();
                        }

                        @SuppressWarnings("unchecked")
                        @NotNull
                        @Override
                        public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                            if(a.length < size())
                                a = Arrays.copyOf(a, size());
                            int i = 0;
                            for(Object key : columnLabelsToIndex.keySet())
                                a[i] = (T) Map.entry(key, Column.this.get(key));
                            return a;
                        }

                        @Override
                        public boolean containsAll(@NotNull Collection<?> c) {
                            for(Object o : c)
                                if(!contains(o))
                                    return false;
                            return true;
                        }
                    };
                }
            };
        }
    }
}
