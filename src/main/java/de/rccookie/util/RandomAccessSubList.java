package de.rccookie.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.RandomAccess;

import org.jetbrains.annotations.NotNull;


/**
 * A general purpose sublist implementation, to be used as implementation of {@link List#subList(int, int)}.
 * All write operation are supported and will be forwarded to the backing list (though maybe not using the
 * same method). If the backing list was structurally modified since the creation of the sublist without
 * using that sublist to do so, any further operations on the sublist are undefined. The implementation
 * throws a {@link ConcurrentModificationException} at best effort.
 * <p>An instance of the class can be created using {@link #ofRange(List, int, int)}</p>
 *
 * @param <T> The content type of the list that this list is a sublist of
 */
public class RandomAccessSubList<T> implements List<T>, RandomAccess {

    private final List<T> list;
    private final int expectedExtraSize;
    private final int start;
    private int end;

    protected RandomAccessSubList(List<T> list, int start, int end) {
        this.list = Arguments.checkNull(list, "list");
        this.start = Arguments.checkRange(start, 0, list.size() + 1);
        this.end = Arguments.checkRange(end, start, list.size() + 1);
        expectedExtraSize = list.size() - (end - start);
    }

    @Override
    public String toString() {
        if(expectedExtraSize != list.size() - (end - start))
            return "<Illegal sublist of "+list+">";
        return Utils.toString(this);
    }

    @Override
    public boolean equals(Object obj) {
        return Utils.equals(this, obj);
    }

    @Override
    public int hashCode() {
        return Utils.hashCode(list, start, end);
    }

    @Override
    public int size() {
        checkMod();
        return end - start;
    }

    @Override
    public boolean isEmpty() {
        checkMod();
        return start == end;
    }

    @Override
    public boolean contains(Object o) {
        return indexOf(o) != -1;
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public Object @NotNull [] toArray() {
        return toArray(new Object[size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <U> U @NotNull [] toArray(@NotNull U @NotNull [] a) {
        checkMod();
        if(a.length < size())
            a = Arrays.copyOf(a, size());
        for(int i=0; i<size(); i++)
            a[i] = (U) get(i);
        return a;
    }

    @Override
    public boolean add(T t) {
        checkMod();
        list.add(end - start, t);
        return recalculateEnd();
    }

    @Override
    public boolean remove(Object o) {
        checkMod();
        for(int i=start; i<end; i++) {
            if(Objects.equals(o, list.get(i))) {
                list.remove(i);
                return recalculateEnd();
            }
        }
        return false;
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for(Object o : c)
            if(!contains(o))
                return false;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        return addAll(end - start, c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        checkMod();
        Arguments.checkRange(index, 0, end - start + 1);
        if(!list.addAll(start + index, c))
            return false;
        return recalculateEnd();
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        checkMod();
        boolean mod = false;
        for(int i=start; i<end; i++) {
            if(c.contains(list.get(i))) {
                //noinspection SuspiciousListRemoveInLoop
                list.remove(i);
                mod |= recalculateEnd();
            }
        }
        return mod;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        checkMod();
        boolean mod = false;
        for(int i=start; i<end; i++) {
            if(!c.contains(list.get(i))) {
                //noinspection SuspiciousListRemoveInLoop
                list.remove(i);
                mod |= recalculateEnd();
            }
        }
        return mod;
    }

    @Override
    public void clear() {
        checkMod();
        while(list.size() > expectedExtraSize)
            list.remove(start);
        end = start;
    }

    @Override
    public T get(int index) {
        checkMod();
        return list.get(Arguments.checkRange(index, 0, end - start) + start);
    }

    @Override
    public T set(int index, T element) {
        checkMod();
        T res = list.set(Arguments.checkRange(index, 0, end - start) + start, element);
        recalculateEnd();
        return res;
    }

    @Override
    public void add(int index, T element) {
        checkMod();
        list.add(start + Arguments.checkRange(index, 0, end - start + 1), element);
        recalculateEnd();
    }

    @Override
    public T remove(int index) {
        checkMod();
        T res = list.remove(index);
        recalculateEnd();
        return res;
    }

    @Override
    public int indexOf(Object o) {
        checkMod();
        for(int i=start; i<end; i++)
            if(Objects.equals(o, list.get(i)))
                return i;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        checkMod();
        for(int i=end-1; i>=start; i--)
            if(Objects.equals(o, list.get(i)))
                return i;
        return -1;
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        checkMod();
        return new RandomAccessListIterator<>(this, index);
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        checkMod();
        // Don't short-circuit and return RandomAccessSubList<>(list, start + fromIndex, start + toIndex)
        // to keep notifying this sublist when the given sublist structurally modifies the source list and
        // prevent a concurrent modification exception.
        return new RandomAccessSubList<>(this, fromIndex, toIndex);
    }

    protected final void checkMod() throws ConcurrentModificationException {
        if(list.size() - (end - start) != expectedExtraSize)
            throw new ConcurrentModificationException();
    }

    protected boolean recalculateEnd() {
        return end != (end = list.size() - expectedExtraSize);
    }


    /**
     * Creates a sublist of the given list viewing the specified range. If the range exactly specifies
     * the whole range of the given list, the list itself will be returned. Otherwise, a new instance
     * of {@link RandomAccessSubList} will be returned.
     *
     * @param list The list to create a sublist for
     * @param fromIndex The first index to be in the sublist
     * @param toIndex The first index to not be in the sublist anymore
     * @return An instance of {@link RandomAccessSubList} or the list itself
     */
    public static <T> List<T> ofRange(List<T> list, int fromIndex, int toIndex) {
        if(fromIndex == 0 && toIndex == list.size())
            return list;
        return new RandomAccessSubList<>(list, fromIndex, toIndex);
    }
}
