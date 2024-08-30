package de.rccookie.util.persistent;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

public interface PersistentList<T> extends List<T>, PersistentData<List<T>> {

    @Override
    default int size() {
        return readLocked(List::size);
    }

    @Override
    default boolean isEmpty() {
        return readLocked(Collection::isEmpty);
    }

    @Override
    default boolean contains(Object o) {
        return readLocked(c -> c.contains(o));
    }

    @NotNull
    @Override
    default Iterator<T> iterator() {
        return new PersistentIteratorView<>(this, Collection::iterator);
    }

    @NotNull
    @Override
    default Object @NotNull [] toArray() {
        return readLocked(Collection::toArray);
    }

    @NotNull
    @Override
    default <U> U @NotNull [] toArray(@NotNull U @NotNull [] a) {
        return readLocked(c -> c.toArray(a));
    }

    @Override
    default boolean add(T k) {
        return testWriteLocked(c -> c.add(k));
    }

    @Override
    default boolean remove(Object o) {
        return testWriteLocked(c -> c.remove(o));
    }

    @Override
    default boolean containsAll(@NotNull Collection<?> c) {
        if(c.isEmpty()) return true;
        //noinspection SlowListContainsAll
        return readLocked(col -> col.containsAll(c));
    }

    @Override
    default boolean addAll(@NotNull Collection<? extends T> c) {
        if(c.isEmpty()) return false;
        return testWriteLocked(col -> col.addAll(c));
    }

    @Override
    default boolean retainAll(@NotNull Collection<?> c) {
        return testWriteLocked(col -> col.retainAll(c));
    }

    @Override
    default boolean removeAll(@NotNull Collection<?> c) {
        return testWriteLocked(col -> col.removeAll(c));
    }

    @Override
    default void clear() {
        testWriteLocked(col -> {
            if(col.isEmpty())
                return false;
            col.clear();
            return true;
        });
    }

    @Override
    default Spliterator<T> spliterator() {
        return new PersistentSpliteratorView<>(this, Collection::spliterator);
    }

    @Override
    default Stream<T> stream() {
        return StreamSupport.stream(spliterator(), false);
    }

    @Override
    default Stream<T> parallelStream() {
        return StreamSupport.stream(spliterator(), true);
    }

    @Override
    default <U> U[] toArray(IntFunction<U[]> generator) {
        return readLocked(c -> c.toArray(generator));
    }

    @Override
    default boolean removeIf(Predicate<? super T> filter) {
        return testWriteLocked(c -> c.removeIf(filter));
    }

    @Override
    default void forEach(Consumer<? super T> action) {
        doReadLocked(c -> c.forEach(action));
    }

    @Override
    default boolean addAll(int index, @NotNull Collection<? extends T> c) {
        if(c.isEmpty()) return false;
        return testWriteLocked(l -> l.addAll(index, c));
    }

    @Override
    default void replaceAll(UnaryOperator<T> operator) {
        doWriteLocked(l -> l.replaceAll(operator));
    }

    @Override
    default void sort(Comparator<? super T> c) {
        doWriteLocked(l -> l.sort(c));
    }

    @Override
    default T get(int index) {
        return readLocked(l -> l.get(index));
    }

    @Override
    default T set(int index, T element) {
        return writeLocked(l -> l.set(index, element));
    }

    @Override
    default void add(int index, T element) {
        doWriteLocked(l -> l.add(index, element));
    }

    @Override
    default T remove(int index) {
        return writeLocked(l -> l.remove(index));
    }

    @Override
    default int indexOf(Object o) {
        return readLocked(l -> l.indexOf(o));
    }

    @Override
    default int lastIndexOf(Object o) {
        return readLocked(l -> l.lastIndexOf(o));
    }

    @NotNull
    @Override
    default ListIterator<T> listIterator() {
        return new PersistentListIteratorView<>(this, List::listIterator);
    }

    @NotNull
    @Override
    default ListIterator<T> listIterator(int index) {
        return new PersistentListIteratorView<>(this, l -> l.listIterator(index));
    }

    @NotNull
    @Override
    default List<T> subList(int fromIndex, int toIndex) {
        return new PersistentListView<>(this, l -> l.subList(fromIndex, toIndex));
    }
}
