package de.rccookie.util;

import java.util.Arrays;
import java.util.Iterator;
import java.util.Objects;

import org.jetbrains.annotations.NotNull;

/**
 * A simple ring buffer with queue-like write access, but random read access.
 * No out of range checks!
 *
 * @param <T> Content type.
 */
public class RingBuffer<T> implements Iterable<T> {

    /**
     * The actual data.
     */
    protected Object[] data;
    /**
     * Head and tail pointer inside the data buffer; head points at the next free
     * index, tail at the oldest item. If <code>head == tail</code> the buffer is empty.
     */
    protected int head = 0, tail = 0;

    /**
     * Creates a new ring buffer with a default capacity of 4 that will grow as needed.
     */
    public RingBuffer() {
        data = new Object[4];
    }

    /**
     * Creates a new ring buffer.
     * @param minDefaultCapacity The minimum capacity the buffer should have (the capacity will still grow if needed, only for performance). The actual capacity may be greater though.
     */
    public RingBuffer(int minDefaultCapacity) {
        Arguments.checkRange(minDefaultCapacity, 1, null);
        int cap = 2;
        while(cap <= minDefaultCapacity) cap <<= 1;
        data = new Object[cap];
    }

    @Override
    public String toString() {
        if(head == tail) return "[]";
        StringBuilder str = new StringBuilder().append('[');

        str.append(data[tail]);
        for(int i=(tail+1)&(data.length-1); i!=head; i = (i+1)&(data.length-1))
            str.append(", ").append(data[i]);
        return str.append(']').toString();
    }

    @Override
    public int hashCode() {
        // Equivalent to Arrays.hashCode(Object[]) on range
        if(head == tail) return 1;

        int result = 1;
        for(int i=tail; i!=head; i = (i+1)&(data.length-1))
            result = 31 * result + (data[i] == null ? 0 : data[i].hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj == this) return true;
        if(!(obj instanceof RingBuffer)) return false;

        RingBuffer<?> b = (RingBuffer<?>) obj;
        int size = size();
        if(size != b.size()) return false;

        for(int i=0; i<size; i++)
            if(!Objects.equals(get(i), b.get(i))) return false;
        return true;
    }

    /**
     * Returns an array with the contents of the ring buffer, where the element at index 0 is the oldest item.
     *
     * @return This buffer as array
     */
    public Object[] toArray() {
        Object[] arr = new Object[size()];
        for(int i=0; i<arr.length; i++)
            arr[i] = get(i);
        return arr;
    }

    /**
     * Returns an array with the contents of the ring buffer, where the element at index 0 is the oldest item.
     * If the given array is big enough to fit the contents of this buffer, the data will be written to the
     * first <code>size()</code> elements of it, otherwise a new array with sufficient capacity of the same type
     * will be allocated and the given array will not be modified.
     *
     * @param arr The array to write into or to use as array generator
     * @return The array containing the buffer's data
     */
    @SuppressWarnings("unchecked")
    public <U> U[] toArray(U[] arr) {
        int size = size();
        if(arr.length < size)
            arr = Arrays.copyOf(arr, size);
        for(int i=0; i<size; i++)
            arr[i] = (U) get(i);
        return arr;
    }

    /**
     * Returns an iterator over this buffer, in the order oldest to newest item. The buffer must not be modified
     * (also not using {@link #optimize()}) while the iterator is used, otherwise the behavior of the iterator
     * is undefined.
     *
     * @return An iterator over the contents of this buffer
     */
    @NotNull
    @Override
    public Iterator<T> iterator() {
        return new Iterator<>() {
            int i = tail;
            @Override
            public boolean hasNext() {
                return i != head;
            }

            @SuppressWarnings("unchecked")
            @Override
            public T next() {
                if(i == head) throw new EmptyIteratorException();
                T t = (T) data[i];
                i = (i+1) & (data.length+1);
                return t;
            }
        };
    }

    /**
     * Returns whether the buffer is empty.
     *
     * @return Whether the buffer contains no elements
     */
    public boolean isEmpty() {
        return head == tail;
    }

    /**
     * Returns the number of elements currently in the buffer.
     *
     * @return The current size of the buffer
     */
    public int size() {
        return (head + data.length - tail) & (data.length-1);
    }

    /**
     * Returns, but does not remove the next element in the buffer, that is, the
     * element that has been in the buffer for the longest amount of time. Equivalent
     * to <code>get(0)</code>.
     *
     * @return The next element
     */
    @SuppressWarnings("unchecked")
    public T get() {
        return (T) data[tail];
    }

    /**
     * Returns, but does not remove the element at the given index in the buffer. The
     * element with index 0 is the element in the buffer for the longest amount of time,
     * the element with index <code>size() - 1</code> is the element inserted the most
     * lately.
     *
     * @param index The index of the element to get.
     * @return The element at that index
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        return (T) data[(tail + index) & (data.length-1)];
    }

    /**
     * Appends the given element to this buffer, growing its capacity if needed.
     *
     * @param t The element to append
     * @return The element inserted, in other words, <code>t</code>
     */
    public T add(T t) {
        if(((head + 1) & (data.length-1)) == tail) {
            Object[] newData = new Object[data.length << 1];
            if(tail == 0)
                System.arraycopy(data, 0, newData, 0, data.length - 1);
            else {
                System.arraycopy(data, tail, newData, tail, data.length - tail);
                System.arraycopy(data, 0, newData, data.length, head);
                head += data.length;
            }
            data = newData;
        }
        data[head] = t;
        head = (head + 1) & (data.length-1);
        return t;
    }

    /**
     * Appends all the given elements to this buffer, growing its capacity if needed. Equivalent,
     * but possibly faster than calling {@link #add(T)} for each item individually.
     *
     * @param ts The items to be appended
     */
    @SuppressWarnings("unchecked")
    public void add(T... ts) {
        int size = size();
        int newCap = data.length;
        while(newCap <= size + ts.length) newCap <<= 1;

        if(newCap != data.length) {
            realloc(newCap, size); // Shifts tail to 0, there will be no wraparound
            System.arraycopy(ts, 0, data, head, ts.length);
            head += ts.length; // head must be less than data.length because we required one extra space (which must be there to differ from being empty)
        }
        else if(head + ts.length <= data.length) { // Inserted block is a single block, does not wrap around
            System.arraycopy(ts, 0, data, head, ts.length);
            head = (head + ts.length) & (data.length-1); // Data will not wrap around, but head might be first index out of bounds -> wrap back to 0
        }
        else { // Inserted block is split because it wraps around
            System.arraycopy(ts, 0, data, head, data.length - head); // First segment, fill until end
            System.arraycopy(ts, data.length - head, data, 0, ts.length - data.length + head); // Second segment, wrapped around from 0
            head = head + ts.length - data.length;
        }
    }

    /**
     * Removes the next element from the buffer, that is, the element that has been
     * inserted the longest time ago.
     *
     * @return The removed element.
     */
    @SuppressWarnings("unchecked")
    public T remove() {
        T value = (T) data[tail];
        tail = (tail + 1) & (data.length-1);
        return value;
    }

    /**
     * Removes the given number of oldest items or as many as the buffer contains.
     *
     * @param maxCount The number of elements to remove, all elements will be removed if there are not as many items in the buffer
     * @return The number of actually removed items
     */
    public int remove(int maxCount) {
        int size = size();
        if(maxCount > size) {
            head = tail = 0;
            return size;
        }
        tail = (tail + maxCount) & (data.length-1);
        return maxCount;
    }

    /**
     * Removes all items from the buffer.
     */
    public void clear() {
        head = tail = 0;
    }

    /**
     * Decreases the internal buffer capacity if possible, without any change to the data. The capacity will be
     * increased again if needed later. Only for optimization purposes.
     */
    public void optimize() {
        if(data.length == 2) return;
        if(head == tail || ((tail+1) & (data.length-1)) == head) {
            data = new Object[] { data[0], null }; // If empty then data[0] is irrelevant
            return;
        }

        int size = size();
        int newCap = data.length;
        while(newCap > 2 && newCap > size) newCap--; // '>' not '>=' because one index is always unused

        if(newCap != data.length)
            realloc(newCap, size);
    }

    private void realloc(int newCap, int size) {
        Object[] newData = new Object[newCap];
        if(tail < head)
            System.arraycopy(data, tail, newData, 0, size);
        else {
            System.arraycopy(data, tail, newData, 0, data.length - tail);
            System.arraycopy(data, 0, newData, data.length - tail, size - data.length + tail);
        }
        data = newData;
        tail = 0;
        head = size;
    }
}
