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

import org.jetbrains.annotations.NotNull;

/**
 * A lazily populated list that can also be used as stream.
 *
 * <p>If the first operation used on the instance is a intermediate stream operation,
 * the list functionality gets disabled. This avoids that when using many intermediate
 * operations on a stream the contents will be buffered in each list without being used.
 * Once a list operation or a stream terminal operation have been used, all methods of
 * the instance can safely be reused as often as possible. This also includes any stream
 * operations, the result will always be consistent. The {@link #useAsList()} method is
 * designed as a list operation that has no action to indicate that the list should be
 * buffered even if the first (next) operation is an intermediate stream operation.</p>
 *
 * @param <T> Content type of the list
 */
public class ListStream<T> implements List<T>, Stream<T> {

    private final Iterator<T> iterator;
    private List<T> buffer;
    private Runnable onClose = null;
    private boolean closed = false;
    private boolean listUsed = false;

    private ListStream(List<T> buffer) {
        this.buffer = Arguments.checkNull(buffer);
        this.iterator = IterableIterator.empty();
        listUsed = true; // Don't clear the backing list when stream is used
    }

    private ListStream(Iterator<T> iterator) {
        this.iterator = Arguments.checkNull(iterator);
        buffer = new ArrayList<>();
    }

    private ListStream(Stream<T> stream) {
        this(stream.iterator());
        onClose = () -> {
            if(!listUsed) stream.close();
        };
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
        useAsList();
        return buffer.isEmpty() && !iterator.hasNext();
    }

    @Override
    public boolean contains(Object o) {
        useAsList();
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
        useAsList();
        for(T t : this)
            action.accept(t);
        // Don't close, also in List API
    }

    @Override
    public boolean isParallel() {
        checkStreamUse();
        return false;
    }

    @Override
    public ListStream<T> sequential() {
        checkStreamUse(); // Don't close because it's used again
        return this;
    }

    @Override
    public ListStream<T> parallel() {
        checkStreamUse();
        return this;
    }

    @Override
    public ListStream<T> unordered() {
        checkStreamUse();
        return this;
    }

    @Override
    public ListStream<T> onClose(Runnable closeHandler) {
        checkStreamUse();
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
        // Don't check whether close is already true; also used to indicate that
        // an intermediate operation has started. onClose won't be called twice
        // because it gets reset
        if(!listUsed) {
            closed = true;
            buffer = null;
        }
        if(onClose != null) onClose.run();
        onClose = null;
    }

    @Override
    public ListStream<T> filter(Predicate<? super T> predicate) {
        return subStream(new FilteringIterator<>(streamIterator(), predicate));
    }

    public <R> ListStream<R> filterType(Class<R> type) {
        Arguments.checkNull(type, "type");
        return filter(type::isInstance).map(type::cast);
    }

    @Override
    public <R> ListStream<R> map(Function<? super T, ? extends R> mapper) {
        return subStream(new MappingIterator<>(streamIterator(), mapper));
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
        return subStream(new FlatMappingIterator<>(streamIterator(), t -> mapper.apply(t).iterator()));
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
        return subStream(new DistinctIterator<>(streamIterator()));
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
        return subStream(new PeekingIterator<>(streamIterator(), action));
    }

    @Override
    public ListStream<T> limit(long maxSize) {
        return subStream(new LimitingIterator<>(streamIterator(), maxSize));
    }

    @Override
    public ListStream<T> skip(long n) {
        Iterator<T> iterator = streamIterator();
        for(long i=0; i<n && iterator.hasNext(); i++)
            iterator.next();
        return subStream(iterator);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        checkStreamUse();
        for(T t : this)
            action.accept(t);
        close();
    }

    @Override
    public Object[] toArray() {
        useAsList();
        return buffer.toArray();
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        checkStreamUse();
        T result = identity;
        for(T t : this)
            result = accumulator.apply(result, t);
        close();
        return result;
    }

    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        checkStreamUse();
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
        checkStreamUse();
        U result = identity;
        for(T t : this)
            result = accumulator.apply(result, t);
        close();
        return result;
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        checkStreamUse();
        R result = supplier.get();
        for(T t : this)
            accumulator.accept(result, t);
        close();
        return result;
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        checkStreamUse();
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
        return collect(Collectors.minBy(comparator));
    }

    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        //noinspection SimplifyStreamApiCallChains
        return collect(Collectors.maxBy(comparator));
    }

    @Override
    public long count() {
        checkStreamUse();
        int result = size();
        close();
        return result;
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        checkStreamUse();
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
        checkStreamUse();
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
        checkStreamUse();
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
    public <U> U[] toArray(U @NotNull [] a) {
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
        for(T t : this) {
            if(Objects.equals(o, t)) return true;
            else buffer.add(t);
        }
        return false;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        useAsList();
        for(Object o : c)
            if(!contains(o)) return false;
        return true;
    }

    @Override
    public boolean addAll(@NotNull Collection<? extends T> c) {
        bufferAll();
        return buffer.addAll(c);
    }

    @Override
    public boolean addAll(int index, @NotNull Collection<? extends T> c) {
        ensureBuffered(index - 1);
        return buffer.addAll(index, c);
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        useAsList();
        boolean out = false;
        for(Object o : c) out |= remove(o);
        return out;
    }

    @Override
    public boolean retainAll(@NotNull Collection<?> c) {
        bufferAll();
        return buffer.retainAll(c);
    }

    @Override
    public void clear() {
        useAsList();
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
        for(T t : this) { // Indicates list usage
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
        useAsList();
        for(int i=0, stop=index-buffer.size()+1; i<stop; i++) {
            if(!iterator.hasNext()) break; // Don't throw exception when directly after last element
            buffer.add(iterator.next());
        }

        return new IterableListIterator<>() {
            final ListIterator<T> bufferIt = buffer.listIterator(index); // Will check bounds

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
        // Don't use spliterator(); indicates use as list
        return StreamSupport.stream(Spliterators.spliteratorUnknownSize(streamIterator(), 0), false);
    }

    private Iterator<T> streamIterator() {
        useAsStream();
        return listUsed ? iterator() : iterator;
    }

    private void ensureBuffered(int index) {
        useAsList();
        for(int i=0, stop=index-buffer.size()+1; i<stop; i++) {
            if(!iterator.hasNext()) throw new IndexOutOfBoundsException(index);
            buffer.add(iterator.next());
        }
    }

    private void bufferAll() {
        useAsList();
        iterator.forEachRemaining(buffer::add);
    }

    /**
     * Indicates that this list stream is used as list. Thus, the internal buffer will
     * not be cleared when a stream operation is used. Using any list operation also causes
     * the same effect, but this method has no further side effects and unwanted computations.
     *
     * @return This list stream
     * @throws IllegalStateException If an intermediate stream operation has already been used
     *                               as the first operation
     */
    public ListStream<T> useAsList() {
        if(closed) throw new IllegalStateException("Intermediate operation of stream has already been used, cannot operate as list anymore");
        listUsed = true;
        return this;
    }

    private void checkStreamUse() {
        if(closed) throw new IllegalStateException("Stream closed");
    }

    private void useAsStream() {
        checkStreamUse();
        if(listUsed) return;
        closed = true;
        buffer = null;
    }



    public static <T> ListStream<T> of(Stream<T> stream) {
        return new ListStream<>(stream);
    }

    public static <T> ListStream<T> of(Iterator<T> iterator) {
        return new ListStream<>(iterator);
    }

    public static <T> ListStream<T> of(Iterable<T> iterable) {
        return new ListStream<>(iterable.iterator());
    }

    @SafeVarargs
    public static <T> ListStream<T> of(T... contents) {
        return new ListStream<>(List.of(contents));
    }
}
