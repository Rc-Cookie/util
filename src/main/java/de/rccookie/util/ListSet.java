package de.rccookie.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

public class ListSet<T> implements List<T>, Set<T>, Cloneable<ListSet<T>> {

    private final List<T> list;
    private final Set<T> set;

    private ListSet(List<T> list, Set<T> set) {
        this.list = list;
        this.set = set;
    }

    public ListSet() {
        this(new ArrayList<>(), new HashSet<>());
    }

    public ListSet(Collection<T> c) {
        this();
        addAll(c);
    }

    @Override
    public @NotNull ListSet<T> clone() {
        ListSet<T> clone = new ListSet<>();
        clone.list.addAll(list);
        clone.set.addAll(set);
        return clone;
    }

    @Override
    public int size() {
        return list.size();
    }

    @Override
    public boolean isEmpty() {
        return list.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return set.contains(o);
    }

    @NotNull
    @Override
    public Iterator<T> iterator() {
        return list.iterator();
    }

    @NotNull
    @Override
    public Object @NotNull [] toArray() {
        return list.toArray();
    }

    @NotNull
    @Override
    public <U> U @NotNull [] toArray(@NotNull U @NotNull [] a) {
        return list.toArray(a);
    }

    @Override
    public boolean add(T t) {
        if(!set.add(t)) return false;
        list.add(t);
        return true;
    }

    @Override
    public boolean remove(Object o) {
        return set.remove(o) && list.remove(o);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return set.containsAll(c);
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        boolean diff = false;
        for(T t : c) diff |= add(t);
        return diff;
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        if(c.isEmpty()) return false;
        for(T t : c) add(index++, t);
        return true;
    }

    @Override
    public boolean removeAll(@NotNull Collection<?> c) {
        return set.removeAll(c) && list.removeAll(c);
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        return set.retainAll(c) && list.retainAll(c);
    }

    @Override
    public void clear() {
        list.clear();
        set.clear();
    }

    @Override
    public T get(int index) {
        return list.get(index);
    }

    @Override
    public T set(int index, T element) {
        if(index < 0 || index >= size())
            throw new IndexOutOfBoundsException(index);
        if(set.add(element))
            return list.set(index, element);
        int oldIndex = list.indexOf(element);
        if(oldIndex == index) return list.get(index); // It's equal, but maybe a different instance
        if(oldIndex < index) index--;
        list.remove(oldIndex);
        return list.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        if(index < 0 || index > size())
            throw new IndexOutOfBoundsException(index);
        if(set.add(element)) {
            list.add(index, element);
            return;
        }
        int oldIndex = list.indexOf(element);
        if(oldIndex == index) return;
        if(oldIndex < index) index--;
        list.remove(oldIndex);
        list.add(index, element);
    }

    @Override
    public T remove(int index) {
        T removed = list.remove(index);
        set.remove(removed);
        return removed;
    }

    @Override
    public int indexOf(Object o) {
        return list.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @NotNull
    @Override
    public ListIterator<T> listIterator(int index) {
        ListIterator<T> it = list.listIterator(index);
        return new ListIterator<>() {
            @Override
            public boolean hasNext() {
                return it.hasNext();
            }

            @Override
            public T next() {
                return it.next();
            }

            @Override
            public boolean hasPrevious() {
                return it.hasPrevious();
            }

            @Override
            public T previous() {
                return it.previous();
            }

            @Override
            public int nextIndex() {
                return it.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it.previousIndex();
            }

            @Override
            public void remove() {
                it.remove();
            }

            @Override
            public void set(T t) {
                throw new UnsupportedOperationException();
            }

            @Override
            public void add(T t) {
                throw new UnsupportedOperationException();
            }
        };
    }

    @NotNull
    @Override
    public ListSet<T> subList(int fromIndex, int toIndex) {
        return new ListSet<>(list.subList(fromIndex, toIndex), set);
    }

    @Override
    public Spliterator<T> spliterator() {
        return list.spliterator();
    }

    @Override
    public void sort(Comparator<? super T> c) {
        list.sort(c);
    }

    @Override
    public <U> U[] toArray(IntFunction<U[]> generator) {
        return list.toArray(generator);
    }

    @Override
    public Stream<T> stream() {
        return list.stream();
    }

    @Override
    public Stream<T> parallelStream() {
        return list.parallelStream();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ListSet)
            return list.equals(((ListSet<?>) obj).list);
        return list.equals(obj);
    }

    @Override
    public String toString() {
        return list.toString();
    }
}
