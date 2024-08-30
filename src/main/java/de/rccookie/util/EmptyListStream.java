package de.rccookie.util;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.BinaryOperator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.function.ToDoubleFunction;
import java.util.function.ToIntFunction;
import java.util.function.ToLongFunction;
import java.util.stream.Collector;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

final class EmptyListStream<T> implements ListStream<T> {

    public static final EmptyListStream<?> INSTANCE = new EmptyListStream<>();


    @Override
    public String toString() {
        return "[]";
    }

    @Override
    public boolean equals(Object obj) {
        return List.of().equals(obj);
    }

    @Override
    public int hashCode() {
        return List.of().hashCode();
    }

    @Override
    public int size() {
        return 0;
    }

    @Override
    public boolean isEmpty() {
        return true;
    }

    @Override
    public boolean contains(Object o) {
        return false;
    }

    @Override
    public @NotNull IterableIterator<T> iterator() {
        return IterableIterator.empty();
    }

    @Override
    public <U> U[] toArray(U @NotNull [] a) {
        return a;
    }

    @Override
    public T get(int index) {
        throw new IndexOutOfBoundsException(index);
    }

    @Override
    public int indexOf(Object o) {
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return -1;
    }

    @Override
    public @NotNull IterableListIterator<T> listIterator() {
        return IterableListIterator.empty();
    }

    @Override
    public @NotNull IterableListIterator<T> listIterator(int index) {
        if(index != 0)
            throw new IndexOutOfBoundsException(index);
        return IterableListIterator.empty();
    }

    @Override
    public @NotNull ListStream<T> sequential() {
        return this;
    }

    @Override
    public @NotNull ListStream<T> parallel() {
        return this;
    }

    @Override
    public @NotNull ListStream<T> unordered() {
        return this;
    }

    @Override
    public @NotNull ListStream<T> onClose(Runnable closeHandler) {
        return this;
    }

    @Override
    public void close() {
    }

    @Override
    public ListStream<T> filter(Predicate<? super T> predicate) {
        return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> ListStream<R> map(Function<? super T, ? extends R> mapper) {
        return (ListStream<R>) this;
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return IntStream.empty();
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return LongStream.empty();
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return DoubleStream.empty();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> ListStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return (ListStream<R>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> ListStream<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return (ListStream<R>) this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <R> ListStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        return (ListStream<R>) this;
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return IntStream.empty();
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return LongStream.empty();
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return DoubleStream.empty();
    }

    @Override
    public ListStream<T> distinct() {
        return this;
    }

    @Override
    public ListStream<T> sorted() {
        return this;
    }

    @Override
    public ListStream<T> sorted(Comparator<? super T> comparator) {
        return this;
    }

    @Override
    public ListStream<T> peek(Consumer<? super T> action) {
        return this;
    }

    @Override
    public ListStream<T> skip(long n) {
        return this;
    }

    @Override
    public ListStream<T> limit(long maxSize) {
        return this;
    }

    @Override
    public ListStream<T> takeWhile(Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public ListStream<T> dropWhile(Predicate<? super T> predicate) {
        return this;
    }

    @Override
    public void forEach(Consumer<? super T> action) {
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.emptySpliterator();
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public <U> U[] toArray(IntFunction<U[]> generator) {
        return generator.apply(0);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return identity;
    }

    @NotNull
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return Optional.empty();
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return identity;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return supplier.get();
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return collector.finisher().apply(collector.supplier().get());
    }

    @NotNull
    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return Optional.empty();
    }

    @Override
    public long count() {
        return 0;
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return true;
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return true;
    }

    @NotNull
    @Override
    public Optional<T> findFirst() {
        return Optional.empty();
    }

    @NotNull
    @Override
    public Optional<T> findAny() {
        return Optional.empty();
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        return c.isEmpty();
    }

    @Override
    public @NotNull ListStream<T> subList(int fromIndex, int toIndex) {
        if(fromIndex != 0)
            throw new IndexOutOfBoundsException(fromIndex);
        if(toIndex != 0)
            throw new IndexOutOfBoundsException(toIndex);
        return this;
    }

    @Override
    public ListStream<T> parallelStream() {
        return this;
    }

    @Override
    public ListStream<T> stream() {
        return this;
    }

    @Override
    public ListStream<T> useAsList() {
        return this;
    }

    @Override
    public ListStream<T> toList() {
        return this;
    }
}
