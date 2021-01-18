package com.github.rccookie.common.util;

import java.io.Serializable;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;


/**
 * A line represents a special type of map that contains at most one element
 * at every possible position on the number line. The steps between the
 * elements (and the default step size) may differ between implementations, as
 * subclasses using doubles will be able to define smaller steps than those
 * using integers.
 */
public interface Line<N extends Number, T> extends SortedMap<N,T>, Serializable {

    /**
     * The Comparator all subclasses are encuraged to use, if they need one.
     */
    public static final Comparator<Number> COMPARATOR = (a, b) -> {
        double dif = b.doubleValue() - a.doubleValue();
        if(dif < 0) return -1;
        if(dif > 0) return 1;
        return 0;
    };

    /**
     * Returns a sequential Stream with this line's entrys as its source.
     * 
     * @return a sequential Stream over the entrys in this line
     */
    public default Stream<Entry<N,T>> entryStream() {
        return entrySet().stream();
    }

    /**
     * Returns a sequential Stream with this line's values as its source.
     * 
     * @return a sequential Stream over the elements in this line
     */
    public default Stream<T> stream() {
        return values().stream();
    }

    @Override
    public SortedSet<Map.Entry<N, T>> entrySet();

    /**
     * Creates and returns a list of all elements in this line in proper sequence.
     * If this line supports dublicate elements, then the list will contain
     * dublicate elements multiple times.
     * 
     * @return A list containing the line's elements in proper sequence
     */
    @Override
    public List<T> values();

    @Override
    public SortedSet<N> keySet();

    /**
     * Returns the difference between the smallest and the greatest key in the line.
     * <p>Subclasses may override this method to return a specific type of number.
     * 
     * @return The range of keys
     */
    public default Number range() {
        return lastKey().doubleValue() - firstKey().doubleValue();
    }

    /**
     * Prepends the given element as the first (lowest) element of the line as specified by
     * <pre>nextFree(lastKey())</pre>
     * 
     * @param value The element to prepend
     * @return The index where the element was prepended
     */
    public default N prepend(T value) {
        N i = nextFree(lastKey());
        put(i, value);
        return i;
    }

    /**
     * Appends the given element as the last (highest) element of the line as specified by
     * <pre>nextFreeDown(firstKey())</pre>
     * 
     * @param value The element to append
     * @return The index where the element was appended
     */
    public default N append(T value) {
        N i = nextFreeDown(firstKey());
        put(i, value);
        return i;
    }

    /**
     * Inserts the given value at the specified index and shifts all previous elements up,
     * if neccecary. The 'steps' that are used may differ between different implementations
     * of line.
     * 
     * @param index The index to insert at
     * @param value The element to insert
     * @return {@code true} if the spot to insert in was free
     */
    public default boolean insert(N index, T value) {
        T initial = put(index, value);
        if(initial == null) return true;
        insert(next(index), value);
        return false;
    }

    /**
     * Inserts the given value at the specified index and shifts all previous elements down,
     * if neccecary. The 'steps' that are used may differ between different implementations
     * of line.
     * 
     * @param index The index to insert at
     * @param value The element to insert
     * @return {@code true} if the spot to insert in was free
     */
    public default boolean insertDown(N index, T value) {
        T initial = put(index, value);
        if(initial == null) return true;
        insertDown(nextDown(index), value);
        return false;
    }

    /**
     * Adds the given value at the next free 'spot' upwards including the index itself as
     * specified by
     * <pre>nextFree(index)</pre>
     * 
     * @param index The index to add the value at, or above if occupied
     * @param value The value to add
     * @return The index that the element was actually added in
     */
    public default N add(N index, T value) {
        N i = nextFree(index);
        put(i, value);
        return i;
    }

    /**
     * Adds the given value at the next free 'spot' downwards including the index itself as
     * specified by
     * <pre>nextFreeDown(index)</pre>
     * 
     * @param index The index to add the value at, or below if occupied
     * @param value The value to add
     * @return The index that the element was actually added in
     */
    public default N addDown(N index, T value) {
        N i = nextFree(index);
        put(i, value);
        return i;
    }

    /**
     * Adds the given value at the next free 'spot' upwards starting inclusive at 0 as
     * specified by
     * <pre>nextFree(0)</pre>
     * This method has to be implemented manually to ensure that 0 can be cast to the
     * actual type of number used for indexing and should be as following:
     * <pre>return add(0, value);</pre>
     * 
     * @param value The value to add
     * @return The index that the element was actually added in
     */
    public N add(T value);

    /**
     * Adds the given value at the next free 'spot' downwards starting inclusive at 0 as
     * specified by
     * <pre>nextFree(0)</pre>
     * This method has to be implemented manually to ensure that 0 can be cast to the
     * actual type of number used for indexing and should be as following:
     * <pre>return addDown(0, value);</pre>
     * 
     * @param value The value to add
     * @return The index that the element was actually added in
     */
    public N addDown(T value);

    /**
     * Returns the next free index starting at and including {@code index} upwards. The
     * result of this may differ between different implementation that interprete the
     * default 'steps' between elements differently and strongly depends on the
     * implementation of {@link #next(Number)}.
     * <p>The default implementation works as
     * <pre>
     * if(!containsKey(index)) return index;
     * return nextFree(next(index));
     * </pre>
     * 
     * @param index The index to get the next free index above (inclusive)
     * @return The next free index above (inclusive)
     */
    public default N nextFree(N index) {
        if(!containsKey(index)) return index;
        return nextFree(next(index));
    }

    /**
     * Returns the next free index starting at and including {@code index} downwards. The
     * result of this may differ between different implementation that interprete the
     * default 'steps' between elements differently and strongly depends on the
     * implementation of {@link #nextDown(Number)}.
     * <p>The default implementation works as
     * <pre>
     * if(!containsKey(index)) return index;
     * return nextFreeDown(nextDown(index));
     * </pre>
     * 
     * @param index The index to get the next free index below (inclusive)
     * @return The next free index below (inclusive)
     */
    public default N nextFreeDown(N index) {
        if(!containsKey(index)) return index;
        return nextFreeDown(nextDown(index));
    }

    /**
     * Returns the next position above the index that is considered a 'step' and could be
     * filled automatically using the add, append or insert methods.
     * <p>Implementing subclasses especially for floating point numbers should consider the
     * case that the given index is <b>not</b> in the usual step line and therefore simple
     * adding might not work properly.
     * 
     * @param index The starting index
     * @return The next index above the given one
     */
    public N next(N index);

    /**
     * Returns the next position below the index that is considered a 'step' and could be
     * filled automatically using the addDown, prepend or insertDown methods.
     * <p>Implementing subclasses especially for floating point numbers should consider the
     * case that the given index is <b>not</b> in the usual step line and therefore simple
     * subtracting might not work properly.
     * 
     * @param index The starting index
     * @return The next index below the given one
     */
    public N nextDown(N index);
}
