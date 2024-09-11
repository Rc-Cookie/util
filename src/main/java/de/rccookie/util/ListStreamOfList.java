package de.rccookie.util;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Spliterator;
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
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;

class ListStreamOfList<T> extends ImmutableListView<T> implements ListStream<T> {

//    private static final ListStream2<T> EMPTY =

    private Runnable onClose = null;

    public ListStreamOfList(List<? extends T> list) {
        super(list);
    }

    @Override
    public @NotNull Stream<T> parallel() {
        return parallelStream();
    }

    @Override
    public @NotNull ListStream<T> unordered() {
        return ListStream.of(plainStream().unordered());
    }

    @Override
    public @NotNull ListStream<T> onClose(Runnable closeHandler) {
        // Don't throw an exception on multiple calls; allows to reuse this instance for List#stream()
        onClose = combineOnClose(onClose, closeHandler);
        return this;
    }

    @Override
    public void close() {
        if(onClose != null) try {
            onClose.run();
        } finally {
            onClose = null;
        }
    }

    @Override
    public ListStream<T> filter(Predicate<? super T> predicate) {
        return ListStream.of(plainStream().filter(predicate));
    }

    @Override
    public <R> ListStream<R> map(Function<? super T, ? extends R> mapper) {
        return ListStream.of(plainStream().map(mapper));
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return plainStream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return plainStream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return plainStream().mapToDouble(mapper);
    }

    @Override
    public <R> ListStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return ListStream.of(plainStream().flatMap(mapper));
    }

    @Override
    public <R> ListStream<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        return ListStream.of(plainStream().flatMap(t -> Utils.stream(mapper.apply(t))));
    }

    @Override
    public <R> ListStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        return ListStream.of(plainStream().flatMap(t -> Arrays.stream(mapper.apply(t))));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return plainStream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return plainStream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return plainStream().flatMapToDouble(mapper);
    }

    @Override
    public ListStream<T> distinct() {
        return ListStream.of(plainStream().distinct());
    }

    @Override
    public ListStream<T> sorted() {
        return ListStream.of(plainStream().sorted());
    }

    @Override
    public ListStream<T> sorted(Comparator<? super T> comparator) {
        return ListStream.of(plainStream().sorted(comparator));
    }

    @Override
    public ListStream<T> peek(Consumer<? super T> action) {
        return ListStream.of(plainStream().peek(action));
    }

    @Override
    public ListStream<T> limit(long maxSize) {
        return ListStream.of(plainStream().limit(maxSize));
    }

    @Override
    public ListStream<T> takeWhile(Predicate<? super T> predicate) {
        return ListStream.of(plainStream().takeWhile(predicate));
    }

    @Override
    public ListStream<T> dropWhile(Predicate<? super T> predicate) {
        return ListStream.of(plainStream().dropWhile(predicate));
    }

    @Override
    public ListStream<T> skip(long n) {
        if(list.size() <= n)
            return ListStream.empty();
        return ListStream.of(list.subList((int) n, list.size()));
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        list.forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        list.forEach(action);
    }

    @SuppressWarnings("unchecked")
    @Override
    @NotNull
    public Spliterator<T> spliterator() {
        return (Spliterator<T>) list.spliterator();
    }

    @Override
    public <U> U @NotNull [] toArray(IntFunction<U[]> generator) {
        return list.toArray(generator);
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        T result = identity;
        for(T t : list)
            result = accumulator.apply(result, t);
        return result;
    }

    @NotNull
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        boolean first = true;
        T result = null;
        for(T t : list) {
            if(first) {
                first = false; // Relevant if returned value is also null
                result = t;
            }
            else result = accumulator.apply(result, t);
        }
        return Optional.ofNullable(result);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        U result = identity;
        for(T t : list)
            result = accumulator.apply(result, t);
        return result;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        R result = supplier.get();
        for(T t : list)
            accumulator.accept(result, t);
        return result;
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        A result = collector.supplier().get();
        BiConsumer<A, ? super T> accumulator = collector.accumulator();
        for(T t : list)
            accumulator.accept(result, t);
        return collector.finisher().apply(result);
    }

    @NotNull
    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        boolean first = true;
        T result = null;
        for(T t : list) {
            if(first) {
                first = false; // Relevant if returned value is also null
                result = t;
            }
            else if(comparator.compare(result, t) > 0)
                result = t;
        }
        return Optional.ofNullable(result);
    }

    @NotNull
    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        boolean first = true;
        T result = null;
        for(T t : list) {
            if(first) {
                first = false; // Relevant if returned value is also null
                result = t;
            }
            else if(comparator.compare(result, t) < 0)
                result = t;
        }
        return Optional.ofNullable(result);
    }

    @Override
    public long count() {
        return size();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        for(T t : list)
            if(predicate.test(t)) return true;
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        for(T t : list)
            if(!predicate.test(t)) return false;
        return true;
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return !anyMatch(predicate);
    }

    @NotNull
    @Override
    public Optional<T> findFirst() {
        return list.isEmpty() ? Optional.empty() : Optional.of(first());
    }

    @NotNull
    @Override
    public Optional<T> findAny() {
        return findFirst();
    }

    @Override
    public @NotNull ListStream<T> subList(int fromIndex, int toIndex) {
        return new ListStreamOfList<>(list.subList(fromIndex, toIndex));
    }

    @SuppressWarnings("unchecked")
    @Override
    public Stream<T> parallelStream() {
        return (Stream<T>) list.parallelStream();
    }

    @Override
    public ListStream<T> stream() {
        return this; // Fine because this stream cannot be consumed
    }

    @Override
    public ListStream<T> useAsList() {
        return this; // Nothing to do
    }

    @Override
    public ListStream<T> toList() {
        return this; // Already buffered
    }


    @SuppressWarnings("unchecked")
    private Stream<T> plainStream() {
        Stream<T> stream = (Stream<T>) list.stream();
        if(!(stream instanceof ListStreamOfList) || ((ListStreamOfList<?>) stream).list != list)
            return stream;
        // Handle special case where list implementation return ListStream.of(this) for stream():
        // If we just return list.stream(), we get another ListStreamOfList over the same list, which
        // will again return a ListStreamOfList for plainStream() and so on, causing a stack overflow

        // This alternative works unless spliterator() is defined as stream().spliterator(), or
        // indirectly over stream().iterator().

        //noinspection SimplifyStreamApiCallChains
        return (Stream<T>) StreamSupport.stream(list.spliterator(), false);
    }


    static Runnable combineOnClose(Runnable a, Runnable b) {
        if(a == null) return b;
        return () -> {
            try {
                a.run();
            } catch(RuntimeException e) {
                try {
                    b.run();
                } catch(Exception f) {
                    e.addSuppressed(f);
                }
                throw e;
            }
            b.run();
        };
    }
}
