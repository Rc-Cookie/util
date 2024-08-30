package de.rccookie.util;

import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.IntFunction;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A lazily populated immutable list that can also be used as stream.
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
public interface ListStream<T> extends ImmutableList<T>, Stream<T> {

    @NotNull
    @Override
    IterableIterator<T> iterator();

    @NotNull
    @Override
    IterableListIterator<T> listIterator();

    @NotNull
    @Override
    IterableListIterator<T> listIterator(int index);

    @NotNull
    @Override
    ListStream<T> sequential();

    @NotNull
    @Override
    ListStream<T> parallel();

    @NotNull
    @Override
    ListStream<T> unordered();

    @NotNull
    @Override
    ListStream<T> onClose(Runnable closeHandler);

    @Override
    ListStream<T> filter(Predicate<? super T> predicate);

    @Override
    <R> ListStream<R> map(Function<? super T, ? extends R> mapper);

    @Override
    <R> ListStream<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper);

    <R> ListStream<R> flatMapIterable(Function<? super T, ? extends Iterable<? extends R>> mapper);

    <R> ListStream<R> flatMapArray(Function<? super T, ? extends R[]> mapper);

    @Override
    ListStream<T> distinct();

    @Override
    ListStream<T> sorted();

    @Override
    ListStream<T> sorted(Comparator<? super T> comparator);

    @Override
    ListStream<T> peek(Consumer<? super T> action);

    @Override
    ListStream<T> skip(long n);

    @Override
    ListStream<T> limit(long maxSize);

    @Override
    ListStream<T> takeWhile(Predicate<? super T> predicate);

    @Override
    ListStream<T> dropWhile(Predicate<? super T> predicate);

    @Override
    void forEach(Consumer<? super T> action);

    @Override
    Spliterator<T> spliterator();

    @Override
    <U> U[] toArray(IntFunction<U[]> generator);

    @Override
    default Object @NotNull [] toArray() {
        return ImmutableList.super.toArray();
    }

    @Override
    @NotNull
    ListStream<T> subList(int fromIndex, int toIndex);

    @Override
    ListStream<T> parallelStream();

    @Override
    ListStream<T> stream();

    /**
     * Returns a new stream consisting only of the elements in this stream which
     * are an instance of the specified class. This will also filter out any
     * <code>null</code> elements.
     * <p>This is a special combination of {@link #filter(Predicate)} and
     * {@link #map(Function)}.</p>
     *
     * @param type The type for elements to
     * @return A stream consisting only of elements of the specified type
     */
    @SuppressWarnings("unchecked")
    default <R> ListStream<R> ofType(Class<R> type) {
        // The cast is not nice, but another mapper type::cast is really unnecessary if we can also just do the cast,
        // after all the stream returned by filter() only returns elements of the type R
        return (ListStream<R>) filter(type::isInstance);
    }

    /**
     * Returns a stream consisting of each element mapped to the value returned by the
     * given mapper which is not <code>null</code>. This is a special combination of
     * {@link #map(Function)} and {@link #filter(Predicate)}.
     *
     * @param mapper A mapper which maps each element of this stream either to the value
     *               for the new stream, or to <code>null</code>
     * @return A new stream consisting of all non-null values returned by the mapper
     */
    default <R> ListStream<@NotNull R> mapNonNull(Function<? super T, ? extends @Nullable R> mapper) {
        return this.<R>map(mapper).filter(Objects::nonNull);
    }

    /**
     * Returns the first element of the list; equivalent to <code>get(0)</code>.
     *
     * @return The first element of this list
     */
    default T first() {
        return get(0);
    }

    /**
     * Returns the last element of the list; equivalent to <code>get(size() - 1)</code>.
     *
     * @return The last element of this list
     */
    default T last() {
        return get(size() - 1);
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
    ListStream<T> useAsList();

    /**
     * Collects this list stream into a list. Unlike {@link #useAsList()} this operation ensures
     * that the underlying stream is consumed (it does not load it lazily), and the return value
     * might, but does not have this list stream itself.
     *
     * @return A fully buffered list stream
     */
    ListStream<T> toList();

    /**
     * Collects this list stream into a set, equivalently to <code>collect(Collectors.toSet())</code>.
     * This is a stream operation, the list stream might be closed after using it if this was the
     * first operation on the list stream.
     *
     * @return A set with the contents of this stream
     */
    default Set<T> toSet() {
        return collect(Collectors.toSet());
    }

    /**
     * Collects this list stream into a map, equivalently to <code>collect(Collectors.toMap(keyExtractor, valueExtractor))</code>.
     * This is a stream operation, the list stream might be closed after using it if this was the
     * first operation on the list stream.
     *
     * @return A map with all elements from this stream mapped to an entry
     */
    default <K,V> Map<K,V> toMap(Function<? super T, ? extends K> keyExtractor, Function<? super T, ? extends V> valueExtractor) {
        return collect(Collectors.toMap(keyExtractor, valueExtractor));
    }

    /**
     * Collects this list stream into a map containing all entries returned by the given function.
     *
     * @param entryExtractor The function to return the entry for each element of this stream
     * @return A map with all elements from this stream mapped to an entry
     */
    default <K,V> Map<K,V> toMap(Function<? super T, ? extends Map.Entry<? extends K, ? extends V>> entryExtractor) {
        return collect(HashMap::new, (map,t) -> {
            Map.Entry<? extends K, ? extends V> entry = entryExtractor.apply(t);
            map.put(entry.getKey(), entry.getValue());
        }, Map::putAll);
    }


    /**
     * Returns a list stream over the contents of the given list. The returned list stream
     * has no significant performance overhead for any list or stream operation over the
     * equivalent function performed on the list directly.
     *
     * @param list The list to stream
     * @return A stream over the contents of that list
     */
    @SuppressWarnings("unchecked")
    static <T> ListStream<T> of(List<? extends T> list) {
        if(list instanceof ListStream) return ((ListStream<T>) list).useAsList(); // If this gets called with a not yet used list stream it shouldn't be consumed, as its passed as list
        if(list.isEmpty()) return empty();
        return new ListStreamOfList<>(list);
    }

    /**
     * Returns a list stream over the contents of the given stream.
     *
     * @param stream The stream to create a list stream for
     * @return A list stream with the same elements as the given stream
     */
    @SuppressWarnings("unchecked")
    static <T> ListStream<T> of(Stream<? extends T> stream) {
        return stream instanceof ListStream ? (ListStream<T>) stream : new ListStreamOfStream<>(stream);
    }

    /**
     * Returns a list stream over the contents of the given collection.
     *
     * @param collection The collection to stream
     * @return A stream over the contents of that collection
     */
    static <T> ListStream<T> of(Collection<? extends T> collection) {
        return of(collection.stream());
    }

    /**
     * Returns a list stream over the contents of the given iterator.
     *
     * @param iterator The iterator to stream
     * @return A stream over the contents of that iterator
     */
    static <T> ListStream<T> of(Iterator<? extends T> iterator) {
        return of(Utils.stream(iterator));
    }

    /**
     * Returns a list stream over the contents of the given iterable.
     *
     * @param iterable The iterable to stream
     * @return A stream over the contents of that iterable
     */
    static <T> ListStream<T> of(Iterable<? extends T> iterable) {
        return of(Utils.stream(iterable));
    }

    /**
     * Returns a list stream over the contents of the given iterable.
     *
     * @param iterable The iterable to stream
     * @return A stream over the contents of that iterable
     */
    static <T> ListStream<T> of(IterableIterator<? extends T> iterable) {
        return of(Utils.stream(iterable));
    }

    /**
     * Returns a list stream over the contents of the given spliterator.
     *
     * @param spliterator The spliterator to stream
     * @return A stream over the contents of that spliterator
     */
    static <T> ListStream<T> of(Spliterator<? extends T> spliterator) {
        return of(StreamSupport.stream(spliterator, false));
    }

    /**
     * Returns an infinite stream over the elements returned by the given function.
     *
     * @param seed The first value in the stream
     * @param next A function which maps a given value to the next value to be in the stream
     * @return An infinite stream over the elements returned by the function
     */
    static <T> ListStream<T> iterate(T seed, UnaryOperator<T> next) {
        return of(Stream.iterate(seed, next));
    }

    /**
     * Returns a stream over the elements returned by the given function.
     *
     * @param seed The first element in the stream
     * @param hasNext For a given value, determines whether there should be a next value in the stream
     * @param next Returns the value after a given value for the stream. Only called with arguments which
     *             matched the <code>hasNext</code> predicate.
     * @return A stream over the elements returned by the function
     */
    static <T> ListStream<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        return of(Stream.iterate(seed, hasNext, next));
    }

    /**
     * Returns a list stream over the contents of the given array. This is equivalent to
     * using <code>ListStream.of(Arrays.asList(contents))</code> and the returned list stream
     * should have no significant performance overhead in any list operation to the list
     * returned by {@link Arrays#asList(Object[])}.
     *
     * @param contents The array to stream
     * @return A stream over the contents of that array
     */
    @SafeVarargs
    static <T> ListStream<T> of(T... contents) {
        if(Arguments.deepCheckNull(contents, "contents").length == 0)
            return empty();
        return new ListStreamOfList<>(Arrays.asList(contents));
    }

    /**
     * Returns an empty list stream.
     *
     * @return An empty list stream
     */
    @SuppressWarnings("unchecked")
    static <T> ListStream<T> of() {
        return (ListStream<T>) EmptyListStream.INSTANCE;
    }

    /**
     * Returns an empty list stream.
     *
     * @return An empty list stream
     */
    static <T> ListStream<T> empty() {
        return of();
    }
}
