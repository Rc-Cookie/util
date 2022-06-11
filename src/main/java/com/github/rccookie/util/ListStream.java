package com.github.rccookie.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
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
import java.util.stream.Collectors;
import java.util.stream.DoubleStream;
import java.util.stream.IntStream;
import java.util.stream.LongStream;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * A lazily populated list with full list and stream functionality.
 *
 * @param <T> Content type of the list
 */
public class ListStream<T> implements List<T>, Stream<T> {

    private final Iterator<T> iterator;
    private final List<T> buffer;
    private Runnable onClose = null;

    private ListStream(List<T> buffer) {
        this.iterator = IterableIterator.empty();
        this.buffer = Arguments.checkNull(buffer);
    }

    private ListStream(Iterator<T> iterator) {
        this.iterator = Arguments.checkNull(iterator);
        buffer = new ArrayList<>();
    }

    private ListStream(Stream<T> stream) {
        this(stream.iterator());
        onClose = stream::close;
    }

    @Override
    public String toString() {
        bufferAll();
        return buffer.toString();
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof List)) return false;
        List<?> that = (List<?>) o;
        return buffer.equals(that);
    }

    @Override
    public int hashCode() {
        return buffer.hashCode();
    }

    @Override
    public int size() {
        bufferAll();
        return buffer.size();
    }

    @Override
    public boolean isEmpty() {
        return buffer.isEmpty() && !iterator.hasNext();
    }

    @Override
    public boolean contains(Object o) {
        if(buffer.contains(o)) return true;
        while(iterator.hasNext()) {
            T t = iterator.next();
            buffer.add(t);
            if(Objects.equals(o, t)) return true;
        }
        return false;
    }

    @Override
    public IterableIterator<T> iterator() {
        return listIterator();
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        List.super.forEach(action);
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public ListStream<T> sequential() {
        return this;
    }

    @Override
    public ListStream<T> parallel() {
        return this;
    }

    @Override
    public ListStream<T> unordered() {
        return this;
    }

    @Override
    public ListStream<T> onClose(Runnable closeHandler) {
        Arguments.checkNull(closeHandler);
        if(onClose == null) onClose = closeHandler;
        else {
            Runnable old = onClose;
            onClose = () -> {
                old.run();
                closeHandler.run();
            };
        }
        return this;
    }

    @Override
    public void close() {
        if(onClose != null) onClose.run();
        onClose = null;
    }

    @Override
    public ListStream<T> filter(Predicate<? super T> predicate) {
        return subStream(new FilteringIterator<>((Iterator<T>) iterator(), predicate));
    }

    @Override
    public <R> ListStream<R> map(Function<? super T, ? extends R> mapper) {
        return subStream(new MappingIterator<>((Iterable<T>) iterator(), mapper));
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return normalStream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return normalStream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return normalStream().mapToDouble(mapper);
    }

    @Override
    public <R> ListStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return subStream(new FlatMappingIterator<>((Iterator<T>) iterator(), t -> mapper.apply(t).iterator()));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return normalStream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return normalStream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return normalStream().flatMapToDouble(mapper);
    }

    @Override
    public ListStream<T> distinct() {
        return subStream(new DistinctIterator<>((Iterator<T>) iterator()));
    }

    @Override
    public ListStream<T> sorted() {
        return new ListStream<>(normalStream().sorted()).onClose(this::close);
    }

    @Override
    public ListStream<T> sorted(Comparator<? super T> comparator) {
        return new ListStream<>(normalStream().sorted(comparator)).onClose(this::close);
    }

    @Override
    public ListStream<T> peek(Consumer<? super T> action) {
        return subStream(new PeekingIterator<>((Iterator<T>) iterator(), action));
    }

    @Override
    public ListStream<T> limit(long maxSize) {
        return subStream(new LimitingIterator<>((Iterator<T>) iterator(), maxSize));
    }

    @Override
    public ListStream<T> skip(long n) {
        Iterator<T> iterator = iterator();
        for(long i=0; i<n && iterator.hasNext(); i++)
            iterator.next();
        return subStream(iterator);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        forEach(action);
        close();
    }

    @Override
    public Object[] toArray() {
        bufferAll();
        close();
        return buffer.toArray();
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        T result = identity;
        for(T t : this)
            result = accumulator.apply(result, t);
        close();
        return result;
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        Iterator<T> iterator = iterator();
        if(!iterator.hasNext()) return Optional.empty();
        T result = iterator.next();
        while(iterator.hasNext())
            result = accumulator.apply(result, iterator.next());
        close();
        return Optional.of(result);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        U result = identity;
        for(T t : this)
            result = accumulator.apply(result, t);
        close();
        return result;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        R result = supplier.get();
        for(T t : this)
            accumulator.accept(result, t);
        close();
        return result;
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        A value = collector.supplier().get();
        BiConsumer<A, ? super T> accumulator = collector.accumulator();
        for(T t : this)
            accumulator.accept(value, t);
        close();
        return collector.finisher().apply(value);
    }

    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        //noinspection SimplifyStreamApiCallChains
        Optional<T> result = collect(Collectors.minBy(comparator));
        close();
        return result;
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        //noinspection SimplifyStreamApiCallChains
        Optional<T> result = collect(Collectors.maxBy(comparator));
        close();
        return result;
    }

    @Override
    public long count() {
        int result = size();
        close();
        return result;
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        for(T t : this) {
            if(predicate.test(t)) {
                close();
                return true;
            }
        }
        close();
        return false;
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        for(T t : this) {
            if(!predicate.test(t)) {
                close();
                return false;
            }
        }
        close();
        return true;
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return !anyMatch(predicate);
    }

    @Override
    public Optional<T> findFirst() {
        Optional<T> result = isEmpty() ? Optional.empty() : Optional.of(first());
        close();
        return result;
    }

    public T first() {
        return get(0);
    }

    @Override
    public Optional<T> findAny() {
        return findFirst();
    }

    @Override
    public <U> U[] toArray(U[] a) {
        bufferAll();
        close();
        //noinspection SuspiciousToArrayCall
        return buffer.toArray(a);
    }

    @Override
    public <U> U[] toArray(IntFunction<U[]> generator) {
        bufferAll();
        close();
        //noinspection SuspiciousToArrayCall
        return buffer.toArray(generator);
    }

    @Override
    public boolean add(T t) {
        bufferAll();
        return buffer.add(t);
    }

    @Override
    public boolean remove(Object o) {
        if(buffer.remove(o)) return true;
        while(iterator.hasNext()) {
            T t = iterator.next();
            if(Objects.equals(o, t)) return true;
            else buffer.add(t);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c)
            if(!contains(o)) return false;
        return true;
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        bufferAll();
        return buffer.addAll(c);
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        ensureBuffered(index - 1);
        return buffer.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        boolean out = false;
        for(Object o : c) out |= remove(o);
        return out;
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        bufferAll();
        return buffer.retainAll(c);
    }

    @Override
    public void clear() {
        buffer.clear();
        iterator.forEachRemaining($ -> {});
    }

    @Override
    public T get(int index) {
        ensureBuffered(index);
        return buffer.get(index);
    }

    @Override
    public T set(int index, T element) {
        ensureBuffered(index);
        return buffer.set(index, element);
    }

    @Override
    public void add(int index, T element) {
        ensureBuffered(index-1);
        buffer.add(index, element);
    }

    @Override
    public T remove(int index) {
        ensureBuffered(index-1);
        if(buffer.size() == index) {
            if(iterator.hasNext()) return iterator.next();
            else throw new IndexOutOfBoundsException(index);
        }
        return buffer.get(index);
    }

    @Override
    public int indexOf(Object o) {
        int i = -1;
        for(T t : this) {
            i++;
            if(Objects.equals(o, t)) break;
        }
        return i;
    }

    @Override
    public int lastIndexOf(Object o) {
        bufferAll();
        return buffer.lastIndexOf(o);
    }

    @Override
    public IterableListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public IterableListIterator<T> listIterator(int index) {
        ensureBuffered(index);

        return new IterableListIterator<>() {
            final ListIterator<T> bufferIt = buffer.listIterator(index);

            @Override
            public boolean hasNext() {
                return bufferIt.hasNext() || iterator.hasNext();
            }

            @Override
            public T next() {
                if(bufferIt.hasNext()) return bufferIt.next();
                T t = iterator.next();
                bufferIt.add(t);
                return t;
            }

            @Override
            public boolean hasPrevious() {
                return bufferIt.hasPrevious();
            }

            @Override
            public T previous() {
                return bufferIt.previous();
            }

            @Override
            public int nextIndex() {
                return bufferIt.nextIndex();
            }

            @Override
            public int previousIndex() {
                return bufferIt.previousIndex();
            }

            @Override
            public void remove() {
                bufferIt.remove();
            }

            @Override
            public void set(T t) {
                bufferIt.set(t);
            }

            @Override
            public void add(T t) {
                bufferIt.add(t);
            }
        };
    }

    @Override
    public ListStream<T> subList(int fromIndex, int toIndex) {
        ensureBuffered(toIndex-1);
        return new ListStream<T>(buffer.subList(fromIndex, toIndex)).onClose(this::close);
    }

    @Override
    public Spliterator<T> spliterator() {
        return Spliterators.spliteratorUnknownSize(iterator(), 0);
    }

    @Override
    public ListStream<T> stream() {
        return new ListStream<>(StreamSupport.stream(spliterator(), false));
    }

    @Override
    public ListStream<T> parallelStream() {
        return stream();
    }

    private <R> ListStream<R> subStream(Iterator<R> iterator) {
        return new ListStream<R>(iterator).onClose(this::close);
    }

    private Stream<T> normalStream() {
        return StreamSupport.stream(spliterator(), false);
    }

    private void ensureBuffered(int index) {
        for(int i=0, stop=index-buffer.size()+1; i<stop; i++) {
            if(!iterator.hasNext()) throw new IndexOutOfBoundsException(index);
            buffer.add(iterator.next());
        }
    }

    private void bufferAll() {
        iterator.forEachRemaining(buffer::add);
    }


    /**
     * Creates a new list lazily populated by the given stream.
     *
     * @param stream The stream to wrap
     * @param <T> Content type of the list
     * @return The list stream
     */
    public static <T> ListStream<T> of(Stream<T> stream) {
        return new ListStream<>(stream);
    }

    /**
     * Creates a new list lazily populated using the given iterator.
     *
     * @param iterator The iterator used to populate the list
     * @param <T> Content type of the list
     * @return The list stream
     */
    public static <T> ListStream<T> of(Iterator<T> iterator) {
        return new ListStream<>(iterator);
    }

    /**
     * Creates a new list with the contents of the given collection.
     *
     * @param contents The contents for the list
     * @param <T> Content type of the list
     * @return The list stream
     */
    public static <T> ListStream<T> of(Collection<T> contents) {
        return new ListStream<>(new ArrayList<>(contents));
    }

    /**
     * Creates a new list with the contents of the given array.
     *
     * @param contents The contents for the list
     * @param <T> Content type of the list
     * @return The list stream
     */
    @SafeVarargs
    public static <T> ListStream<T> of(T... contents) {
        return new ListStream<>(List.of(contents));
    }
}
