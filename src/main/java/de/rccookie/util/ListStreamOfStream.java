package de.rccookie.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
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
import org.jetbrains.annotations.Nullable;

class ListStreamOfStream<T> implements ListStream<T> {

    private boolean consumed = false;
    @NotNull
    private Stream<? extends T> stream;
    private Spliterator<? extends T> notYetRead;
    private boolean mightBeMore = true;
    @Nullable
    private List<T> buffer;

    public ListStreamOfStream(Stream<? extends T> stream) {
        this.stream = Arguments.checkNull(stream, "stream");
    }

    @Override
    public String toString() {
        if(consumed && notYetRead == null) return "<Consumed stream>";
        return bufferAll().toString();
    }

    @Override
    public boolean equals(Object obj) {
        if(consumed && notYetRead == null) return obj == this;
        return bufferAll().equals(obj);
    }

    @Override
    public int hashCode() {
        if(consumed && notYetRead == null)
            return System.identityHashCode(this);
        return bufferAll().hashCode();
    }

    @Override
    public int size() {
        useAsList();
        return (int) Math.min(Integer.MAX_VALUE, count());
    }

    @Override
    public boolean isEmpty() {
        useAsList();
        if(buffer == null) buffer = new ArrayList<>();
        else if(!buffer.isEmpty()) return false;
        return !mightBeMore || !(mightBeMore = notYetRead.tryAdvance(buffer::add));
    }

    @Override
    public boolean contains(Object o) {
        useAsList();
        if(buffer == null) buffer = new ArrayList<>();
        else if(buffer.contains(o)) return true;
        if(!mightBeMore) return false;

        //noinspection AssignmentUsedAsCondition
        while(mightBeMore = notYetRead.tryAdvance(buffer::add))
            if(Objects.equals(buffer.get(buffer.size()-1), o))
                return true;
        return false;
    }

    @Override
    public @NotNull IterableIterator<T> iterator() {
        return listIterator();
    }

    @Override
    public <U> U @NotNull [] toArray(U @NotNull [] a) {
        return bufferAll().toArray(a);
    }

    @Override
    public T get(int index) {
        return ensureBuffered(index).get(index);
    }

    @Override
    public int indexOf(Object o) {
        useAsList();
        if(buffer == null) buffer = new ArrayList<>();
        else {
            int index = buffer.indexOf(o);
            if(index >= 0) return index;
        }
        if(!mightBeMore) return -1;

        //noinspection AssignmentUsedAsCondition
        while(mightBeMore = notYetRead.tryAdvance(buffer::add))
            if(Objects.equals(buffer.get(buffer.size()-1), o))
                return buffer.size() - 1;
        return -1;
    }

    @Override
    public int lastIndexOf(Object o) {
        return bufferAll().lastIndexOf(o);
    }

    @Override
    public @NotNull IterableListIterator<T> listIterator() {
        useAsList();
        return new RandomAccessListIterator<>(this);
    }

    @Override
    public @NotNull IterableListIterator<T> listIterator(int index) {
        useAsList();
        return new RandomAccessListIterator<>(this, index);
    }

    @Override
    public @NotNull ListStream<T> sequential() {
        if(notYetRead != null)
            return stream();
        //noinspection DataFlowIssue
        stream = stream.sequential();
        return this;
    }

    @Override
    public @NotNull ListStream<T> parallel() {
        if(notYetRead != null)
            return parallelStream();
        //noinspection DataFlowIssue
        stream = stream.parallel();
        return this;
    }

    @Override
    public @NotNull ListStream<T> unordered() {
        if(notYetRead != null)
            return ListStream.of(plainStream().unordered());
        stream = stream.unordered();
        return this;
    }

    @Override
    public @NotNull ListStream<T> onClose(Runnable closeHandler) {
        if(notYetRead != null)
            return ListStream.of(plainStream().onClose(closeHandler));
        stream = stream.onClose(closeHandler);
        return this;
    }

    @Override
    public void close() {
        stream.close();
    }

    @Override
    public ListStream<T> filter(Predicate<? super T> predicate) {
        consumed = true;
        return ListStream.of(plainStream().filter(predicate));
    }

    @Override
    public <R> ListStream<R> map(Function<? super T, ? extends R> mapper) {
        consumed = true;
        return ListStream.of(plainStream().map(mapper));
    }

    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        consumed = true;
        return plainStream().mapToInt(mapper);
    }

    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        consumed = true;
        return plainStream().mapToLong(mapper);
    }

    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        consumed = true;
        return plainStream().mapToDouble(mapper);
    }

    @Override
    public <R> ListStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        consumed = true;
        return ListStream.of(plainStream().flatMap(mapper));
    }

    @Override
    public <R> ListStream<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper) {
        consumed = true;
        return ListStream.of(plainStream().flatMap(t -> Utils.stream(mapper.apply(t))));
    }

    @Override
    public <R> ListStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper) {
        consumed = true;
        return ListStream.of(plainStream().flatMap(t -> Arrays.stream(mapper.apply(t))));
    }

    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        consumed = true;
        return plainStream().flatMapToInt(mapper);
    }

    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        consumed = true;
        return plainStream().flatMapToLong(mapper);
    }

    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        consumed = true;
        return plainStream().flatMapToDouble(mapper);
    }

    @Override
    public ListStream<T> distinct() {
        consumed = true;
        return ListStream.of(plainStream().distinct());
    }

    @Override
    public ListStream<T> sorted() {
        consumed = true;
        return ListStream.of(plainStream().sorted());
    }

    @Override
    public ListStream<T> sorted(Comparator<? super T> comparator) {
        consumed = true;
        return ListStream.of(plainStream().sorted(comparator));
    }

    @Override
    public ListStream<T> peek(Consumer<? super T> action) {
        consumed = true;
        return ListStream.of(plainStream().peek(action));
    }

    @Override
    public ListStream<T> limit(long maxSize) {
        consumed = true;
        return ListStream.of(plainStream().limit(maxSize));
    }

    @Override
    public ListStream<T> takeWhile(Predicate<? super T> predicate) {
        consumed = true;
        return ListStream.of(plainStream().takeWhile(predicate));
    }

    @Override
    public ListStream<T> dropWhile(Predicate<? super T> predicate) {
        consumed = true;
        return ListStream.of(plainStream().dropWhile(predicate));
    }

    @Override
    public ListStream<T> skip(long n) {
        consumed = true;
        return ListStream.of(plainStream().skip(n));
    }

    @Override
    public void forEach(Consumer<? super T> action) {
        bufferAll().forEach(action);
    }

    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        bufferAll().forEach(action);
    }

    @NotNull
    @Override
    public Spliterator<T> spliterator() {
        useAsList();
        if(!mightBeMore)
            return buffer != null ? buffer.spliterator() : Spliterators.emptySpliterator();

        int characteristics = Spliterator.IMMUTABLE | Spliterator.ORDERED | // These are always present
                (notYetRead.characteristics() & (Spliterator.SIZED | Spliterator.NONNULL | Spliterator.DISTINCT)); // These only if the source has them

        Iterator<T> it = iterator();
        return new Spliterator<>() {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if(!it.hasNext()) return false;
                action.accept(it.next());
                return true;
            }

            @Override
            public Spliterator<T> trySplit() {
                return null;
            }

            @Override
            public long estimateSize() {
                long remaining = mightBeMore ? notYetRead.estimateSize() : 0;
                int buffered = buffer != null ? buffer.size() : 0;
                return Math.max(Long.MAX_VALUE - buffered, remaining) + buffered; // Prevent overflow
            }

            @Override
            public int characteristics() {
                return characteristics;
            }
        };
    }

    @Override
    public boolean isParallel() {
        return false;
    }

    @Override
    public <U> U @NotNull [] toArray(IntFunction<U[]> generator) {
        return bufferAll().toArray(generator);
    }

    @Override
    public boolean containsAll(@NotNull Collection<?> c) {
        for(Object o : c)
            if(!contains(o)) return false;
        return true;
    }

    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        consumed = true;
        return plainStream().reduce(identity, accumulator);
    }

    @NotNull
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        consumed = true;
        return plainStream().reduce(accumulator);
    }

    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        consumed = true;
        return plainStream().reduce(identity, accumulator, combiner);
    }

    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        consumed = true;
        return plainStream().collect(supplier, accumulator, combiner);
    }

    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        consumed = true;
        return plainStream().collect(collector);
    }

    @NotNull
    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        consumed = true;
        return plainStream().min(comparator);
    }

    @NotNull
    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        consumed = true;
        return plainStream().max(comparator);
    }

    @Override
    public long count() {
        if(notYetRead == null)
            return stream.count();

        if(!mightBeMore)
            return buffer != null ? buffer.size() : 0;

        long remaining = notYetRead.getExactSizeIfKnown();
        if(remaining >= 0)
            return Math.min(Integer.MAX_VALUE, (buffer != null ? buffer.size() : 0) + remaining);
        return bufferAll().size();
    }

    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        consumed = true;
        return plainStream().anyMatch(predicate);
    }

    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        consumed = true;
        return plainStream().allMatch(predicate);
    }

    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        consumed = true;
        return plainStream().noneMatch(predicate);
    }

    @SuppressWarnings("unchecked")
    @NotNull
    @Override
    public Optional<T> findFirst() {
        if(buffer != null && !buffer.isEmpty())
            return Optional.of(buffer.get(0));
        if(notYetRead != null) {
            if(buffer == null) buffer = new ArrayList<>();
            return (mightBeMore && (mightBeMore = notYetRead.tryAdvance(buffer::add))) ?
                    Optional.of(buffer.get(0)) : Optional.empty();
        }
        consumed = true;
        return (Optional<T>) stream.findFirst();
    }

    @NotNull
    @Override
    public Optional<T> findAny() {
        consumed = true;
        return plainStream().findAny();
    }

    @Override
    public @NotNull ListStream<T> subList(int fromIndex, int toIndex) {
        useAsList();
        if(fromIndex < 0) throw new IndexOutOfBoundsException(fromIndex);
        if(toIndex < fromIndex) throw new IndexOutOfBoundsException("fromIndex > toIndex ("+fromIndex+" > "+toIndex+")");
        return ListStream.of(ensureBuffered(toIndex-1).subList(fromIndex, toIndex));
    }

    @Override
    public ListStream<T> parallelStream() {
        return ListStream.of(plainStream(true));
    }

    @Override
    public ListStream<T> stream() {
        useAsList();
        return ListStream.of(plainStream());
    }

    @Override
    public ListStream<T> useAsList() {
        if(notYetRead == null)
            notYetRead = stream.spliterator();
        return this;
    }

    @Override
    public ListStream<T> toList() {
        return ListStream.of(bufferAll()); // ListStreamOfList might be more efficient on subsequent operations
    }

    @NotNull
    private List<T> bufferAll() {
        if(notYetRead == null) {
            try {
                buffer = stream.collect(Collectors.toList());
            } finally {
                notYetRead = Spliterators.emptySpliterator();
                mightBeMore = false;
            }
        }
        else {
            if(buffer == null) buffer = new ArrayList<>();
            notYetRead.forEachRemaining(buffer::add);
            mightBeMore = false;
        }
        return buffer;
    }

    @NotNull
    private List<T> ensureBuffered(int index) {
        if(notYetRead == null)
            notYetRead = stream.spliterator();

        if(buffer == null) buffer = new ArrayList<>();
        if(buffer.size() > index) return buffer;

        long remaining = mightBeMore ? notYetRead.getExactSizeIfKnown() : 0;
        if(remaining != -1 && buffer.size() + remaining <= index)
            throw new IndexOutOfBoundsException(index);

        do if(!(mightBeMore = notYetRead.tryAdvance(buffer::add)))
            throw new IndexOutOfBoundsException(index);
        while(buffer.size() <= index);

        return buffer;
    }

    private Stream<T> plainStream() {
        return plainStream(false);
    }

    @SuppressWarnings("unchecked")
    private Stream<T> plainStream(boolean parallel) {
        if(notYetRead == null) return (Stream<T>) stream; // List part has not yet been used
        if(mightBeMore)
            return StreamSupport.stream(spliterator(), parallel);
        if(buffer == null) return ListStream.empty();
        if(parallel)
            return buffer.parallelStream();
        return buffer.stream();
    }
}
