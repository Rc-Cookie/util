package de.rccookie.util.persistent;

import java.util.Collection;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

public interface PersistentSet<T> extends Set<T>, PersistentData<Set<T>> {

    @Override
    default int size() {
        return readLocked(Collection::size);
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
}
