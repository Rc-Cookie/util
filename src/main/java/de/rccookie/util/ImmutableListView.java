package de.rccookie.util;

import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.NotNull;

public class ImmutableListView<T> extends AbstractImmutableList<T> {

    protected final List<? extends T> list;

    public ImmutableListView(List<? extends T> list) {
        this.list = Arguments.checkNull(list, "list");
    }

    @Override
    public String toString() {
        return list.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return list.equals(obj);
    }

    @Override
    public int hashCode() {
        return list.hashCode();
    }

    @Override
    public T get(int index) {
        return list.get(index);
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
    public IterableListIterator<T> listIterator() {
        return Utils.view(list.listIterator());
    }

    @NotNull
    @Override
    public IterableListIterator<T> listIterator(int index) {
        return Utils.view(list.listIterator(index));
    }

    @NotNull
    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new ImmutableListView<>(list.subList(fromIndex, toIndex));
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
        return list.contains(o);
    }

    @NotNull
    @Override
    public IterableIterator<T> iterator() {
        return Utils.view(list.iterator());
    }

    @Override
    public <U> U @NotNull [] toArray(U @NotNull [] a) {
        return list.toArray(a);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        //noinspection SlowListContainsAll
        return list.containsAll(c);
    }
}
