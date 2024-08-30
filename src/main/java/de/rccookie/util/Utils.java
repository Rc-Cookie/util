package de.rccookie.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.DoublePredicate;
import java.util.function.IntPredicate;
import java.util.function.LongPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import de.rccookie.math.Mathf;
import de.rccookie.util.function.MethodFunction;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

public final class Utils {

    private Utils() {
        throw new UnsupportedOperationException("Utils does not allow instances");
    }



    private static final Iterator<?> EMPTY_ITERATOR = new Iterator<>() {
        @Override
        public boolean hasNext() {
            return false;
        }

        @Override
        public Object next() {
            throw new EmptyIteratorException();
        }
    };



    public static <T> T getAny(Collection<T> coll) {
        return coll.iterator().next();
    }

    public static <T> T getAnyOrNull(Collection<T> coll) {
        Iterator<T> it = coll.iterator();
        return it.hasNext() ? it.next() : null;
    }

    public static <T> T popAny(Collection<T> coll) {
        T t = getAny(coll);
        coll.remove(t);
        return t;
    }



    public static <T> Iterator<T> iterator(T[] array) {
        return new Iterator<>() {
            int i = 0;
            @Override
            public boolean hasNext() {
                return i < array.length;
            }

            @Override
            public T next() {
                if(!hasNext())
                    throw new EmptyIteratorException();
                return array[i++];
            }
        };
    }

    public static <T> Spliterator<T> spliterator(Iterator<? extends T> iterator, long estimatedSize, @MagicConstant(flagsFromClass = Spliterator.class) int characteristics) {
        return new Spliterators.AbstractSpliterator<>(estimatedSize, characteristics) {
            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                if(!iterator.hasNext()) return false;
                action.accept(iterator.next());
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                iterator.forEachRemaining(action);
            }
        };
    }

    public static <T> Spliterator<T> spliterator(Iterable<? extends T> iterable, long estimatedSize, @MagicConstant(flagsFromClass = Spliterator.class) int characteristics) {
        return spliterator(iterable.iterator(), estimatedSize, characteristics);
    }

    public static <T> Spliterator<T> spliterator(IterableIterator<? extends T> iterable, long estimatedSize, @MagicConstant(flagsFromClass = Spliterator.class) int characteristics) {
        return spliterator((Iterator<? extends T>) iterable, estimatedSize, characteristics);
    }



    @SuppressWarnings("unchecked")
    public static <T> Iterator<T> emptyIterator() {
        return (Iterator<T>) EMPTY_ITERATOR;
    }

    public static <T> Iterator<T> reverse(ListIterator<T> it) {
        return new ReverseIterator<>(Arguments.checkNull(it, "it"));
    }

    public static <T> Iterable<T> iterate(Stream<T> stream) {
        return stream::iterator;
    }

    public static <T> Stream<T> stream(Iterator<? extends T> iterator) {
        if(!iterator.hasNext()) return Stream.empty();
        return stream(Spliterators.spliteratorUnknownSize(iterator, Spliterator.ORDERED));
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> stream(Iterable<? extends T> iterable) {
        if(iterable instanceof Collection)
            return ((Collection<T>) iterable).stream();
        return stream(iterable.spliterator());
    }

    public static <T> Stream<T> stream(IterableIterator<? extends T> iterable) {
        return stream(iterable.spliterator());
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> stream(Spliterator<? extends T> spliterator) {
        return (Stream<T>) StreamSupport.stream(spliterator, false);
    }

    @SuppressWarnings("unchecked")
    public static <T> Stream<T> ofType(Stream<?> stream, Class<T> type) {
        return (Stream<T>) stream.filter(type::isInstance);
    }

    @SuppressWarnings("unchecked")
    @Contract("null -> null; !null -> !null")
    public static <T> T deepClone(@Nullable T obj) {
        if(!(obj instanceof java.lang.Cloneable)) return obj;
        Class<T> cls = (Class<T>) obj.getClass();
        if(!cls.isArray()) {
            try {
                return (T) cls.getMethod("clone").invoke(obj);
            } catch (InvocationTargetException | IllegalAccessException e) {
                throw new RuntimeException(e);
            } catch (NoSuchMethodException e) {
                throw new AssertionError(e);
            }
        }
        int l = Array.getLength(obj);

        if(!(obj instanceof Object[])) {
            if(obj instanceof int[]) return (T) Arrays.copyOf((int[]) obj, l);
            if(obj instanceof long[]) return (T) Arrays.copyOf((long[]) obj, l);
            if(obj instanceof double[]) return (T) Arrays.copyOf((double[]) obj, l);
            if(obj instanceof float[]) return (T) Arrays.copyOf((float[]) obj, l);
            if(obj instanceof byte[]) return (T) Arrays.copyOf((byte[]) obj, l);
            if(obj instanceof short[]) return (T) Arrays.copyOf((short[]) obj, l);
            if(obj instanceof char[]) return (T) Arrays.copyOf((char[]) obj, l);
            if(obj instanceof boolean[]) return (T) Arrays.copyOf((boolean[]) obj, l);
            throw new AssertionError();
        }
        Object[] clone = Arrays.copyOf((Object[]) obj, Array.getLength(obj));
        for(int i=0; i<clone.length; i++)
            clone[i] = deepClone(clone[i]);
        return (T) clone;
    }



    @SuppressWarnings("unchecked")
    public static <K,V> Map<K,V> map(K key, V value, Object... more) {
        if((more.length & 1) != 0)
            throw new IllegalArgumentException("Number of keys and values differs");
        Map<K,V> map = new HashMap<>();
        map.put(key, value);
        for(int i=0; i<more.length; i+=2)
            map.put((K) more[i], (V) more[i+1]);
        return map;
    }

    public static <K,V> Map<K,V> mapSafe(Class<K> keyType, Class<V> valueType, Object... keyValuePairs) {
        if((keyValuePairs.length & 1) != 0)
            throw new IllegalArgumentException("Number of keys and values differs");
        Map<K,V> map = new HashMap<>();
        for(int i=0; i<keyValuePairs.length; i+=2)
            map.put(keyType.cast(keyValuePairs[i]), valueType.cast(keyValuePairs[i+1]));
        return map;
    }

    public static <E> Collection<E> view(Collection<? extends E> of) {
        return Collections.unmodifiableCollection(of);
    }

    public static <E> List<E> view(List<? extends E> of) {
        return Collections.unmodifiableList(of);
    }

    public static <E> Set<E> view(Set<? extends E> of) {
        return Collections.unmodifiableSet(of);
    }

    public static <E> SortedSet<E> view(SortedSet<E> of) {
        return Collections.unmodifiableSortedSet(of);
    }

    public static <E> NavigableSet<E> view(NavigableSet<E> of) {
        return Collections.unmodifiableNavigableSet(of);
    }

    public static <K,V> Map<K,V> view(Map<? extends K, ? extends V> of) {
        return Collections.unmodifiableMap(of);
    }

    public static <K,V> SortedMap<K,V> view(SortedMap<K,? extends V> of) {
        return Collections.unmodifiableSortedMap(of);
    }

    public static <K,V> NavigableMap<K,V> view(NavigableMap<K,? extends V> of) {
        return Collections.unmodifiableNavigableMap(of);
    }

    public static <E> List<E> view(List<? extends E> first, List<? extends E> second) {
        return new ListConcatenation<>(Arguments.checkNull(first, "first"), Arguments.checkNull(second, "second"));
    }

    public static <E> Iterable<E> view(Iterable<? extends E> of) {
        return new Iterable<>() {
            @NotNull
            @Override
            public Iterator<E> iterator() {
                return view(of.iterator());
            }

            @SuppressWarnings("unchecked")
            @Override
            public Spliterator<E> spliterator() {
                return (Spliterator<E>) of.spliterator();
            }

            @Override
            public void forEach(Consumer<? super E> action) {
                of.forEach(action);
            }
        };
    }

    public static <E> IterableIterator<E> view(Iterator<? extends E> of) {
        return new IterableIterator<>() {
            @Override
            public boolean hasNext() {
                return of.hasNext();
            }

            @Override
            public E next() {
                return of.next();
            }
        };
    }

    public static <E> IterableIterator<E> view(IterableIterator<? extends E> of) {
        return view((Iterator<? extends E>) of);
    }

    public static <E> IterableListIterator<E> view(ListIterator<? extends E> of) {
        return new IterableListIterator<>() {
            @Override
            public boolean hasPrevious() {
                return of.hasPrevious();
            }

            @Override
            public E previous() {
                return of.previous();
            }

            @Override
            public int nextIndex() {
                return of.nextIndex();
            }

            @Override
            public int previousIndex() {
                return of.previousIndex();
            }

            @Override
            public void remove() {
                throw new ViewModificationException();
            }

            @Override
            public void set(E e) {
                throw new ViewModificationException();
            }

            @Override
            public void add(E e) {
                throw new ViewModificationException();
            }

            @Override
            public boolean hasNext() {
                return of.hasNext();
            }

            @Override
            public E next() {
                return of.next();
            }
        };
    }

    public static <L,V> Table<L,V> view(Table<L,V> table) {
        Arguments.checkNull(table, "table");
        return new ImmutableTable<>() {

            @Override
            public String toString() {
                return table.toString();
            }

            @Override
            public boolean equals(Object obj) {
                return table.equals(obj);
            }

            @Override
            public int hashCode() {
                return table.hashCode();
            }

            @Override
            public List<L> columnLabels() {
                return table.columnLabels();
            }

            @Override
            public List<L> rowLabels() {
                return table.rowLabels();
            }

            @Override
            public @Range(from = 0) int indexOfColumnLabel(Object label) {
                return table.indexOfColumnLabel(label);
            }

            @Override
            public @Range(from = 0) int indexOfRowLabel(Object label) {
                return table.indexOfRowLabel(label);
            }

            @Override
            public @NotNull ListStream<Vector<L,V>> rows() {
                return table.rows().map(Utils::view);
            }

            @Override
            public @NotNull ListStream<Vector<L, V>> columns() {
                return table.columns().map(Utils::view);
            }

            @Override
            public @NotNull Vector<L, V> row(@Range(from = 0) int index) {
                return view(table.row(index));
            }

            @Override
            public @NotNull Vector<L, V> column(@Range(from = 0) int index) {
                return view(table.column(index));
            }

            @Override
            public V value(@Range(from = 0) int row, @Range(from = 0) int column) {
                return table.value(row, column);
            }

            @Override
            public @Range(from = 0) int rowCount() {
                return table.rowCount();
            }

            @Override
            public @Range(from = 0) int columnCount() {
                return table.columnCount();
            }

            @Override
            public @Range(from = 0) int valueCount() {
                return table.valueCount();
            }

            @Override
            public V defaultValue() {
                return table.defaultValue();
            }

            @Override
            public <U> U[][] toArray(Class<U> type) {
                return table.toArray(type);
            }
        };
    }

    public static <L,V> Table.Vector<L,V> view(Table.Vector<L,V> vector) {
        Arguments.checkNull(vector, "vector");
        return new ImmutableTable.ImmutableVector<>() {

            @Override
            public String toString() {
                return vector.toString();
            }

            @Override
            public boolean equals(Object obj) {
                return vector.equals(obj);
            }

            @Override
            public int hashCode() {
                return vector.hashCode();
            }

            @Override
            public L label() {
                return vector.label();
            }

            @Override
            public @Range(from = 0) int index() {
                return vector.index();
            }

            @Override
            public @Range(from = 0) int size() {
                return vector.size();
            }

            @Override
            public V get(int index) {
                return vector.get(index);
            }

            @Override
            public V get(Object label) {
                return vector.get(label);
            }

            @Override
            public boolean contains(Object value) {
                return vector.contains(value);
            }

            @Override
            public @NotNull List<V> asList() {
                return vector.asList();
            }

            @Override
            public @NotNull Map<?, V> asMap() {
                return vector.asMap();
            }

            @NotNull
            @Override
            public Iterator<V> iterator() {
                return vector.iterator();
            }
        };
    }

    public static <E> Set<E> viewMapAsSet(Map<? extends E,?> map) {
        return view(map.keySet());
    }

    public static <K,V> Map<K,V> viewSetAsMap(Set<? extends K> set) {
        return new MapFromSet<>(set);
    }

    @SuppressWarnings({"unchecked", "SuspiciousSystemArraycopy"})
    public static <T> T joinArray(T arr1, T arr2) {
        Arguments.checkNull(arr1, "arr1");
        Arguments.checkNull(arr2, "arr2");
        int l1 = Array.getLength(arr1);
        int l2 = Array.getLength(arr2);
        T out = (T) Array.newInstance(arr1.getClass().getComponentType(), l1 + l2);
        System.arraycopy(arr1, 0, out, 0, l1);
        System.arraycopy(arr2, 0, out, l1, l2);
        return out;
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    public static <T> T joinArrays(T... arrays) {
        Arguments.deepCheckNull(arrays);
        if(arrays.length == 0)
            throw new NoSuchElementException("At least one array to join must be given");
        int totalLen = Mathf.sum(arrays, Array::getLength);
        T out = (T) Array.newInstance(arrays[0].getClass(), totalLen);
        for(int i=0,j=0; i<arrays.length; i++) {
            int len = Array.getLength(arrays[i]);
            //noinspection SuspiciousSystemArraycopy
            System.arraycopy(arrays[i], 0, out, j, len);
            j += len;
        }
        return out;
    }



    public static <T> boolean allMatch(T[] arr, Predicate<? super T> filter) {
        for(T t : arr) if(!filter.test(t)) return false;
        return true;
    }

    public static <T> boolean anyMatch(T[] arr, Predicate<? super T> filter) {
        for(T t : arr) if(filter.test(t)) return true;
        return false;
    }

    public static <T> boolean allNull(T[] arr) {
        for(T t : arr) if(t != null) return false;
        return true;
    }

    public static <T> boolean anyNull(T[] arr) {
        for(T t : arr) if(t == null) return true;
        return false;
    }

    public static boolean allMatch(int[] arr, IntPredicate filter) {
        for(int t : arr) if(!filter.test(t)) return false;
        return true;
    }

    public static boolean anyMatch(int[] arr, IntPredicate filter) {
        for(int t : arr) if(filter.test(t)) return true;
        return false;
    }

    public static boolean allMatch(int[] arr, int value) {
        for(int t : arr) if(t != value) return false;
        return true;
    }

    public static boolean anyMatch(int[] arr, int value) {
        for(int t : arr) if(t == value) return true;
        return false;
    }

    public static boolean allMatch(long[] arr, LongPredicate filter) {
        for(long t : arr) if(!filter.test(t)) return false;
        return true;
    }

    public static boolean anyMatch(long[] arr, LongPredicate filter) {
        for(long t : arr) if(filter.test(t)) return true;
        return false;
    }

    public static boolean allMatch(long[] arr, long value) {
        for(long t : arr) if(t != value) return false;
        return true;
    }

    public static boolean anyMatch(long[] arr, long value) {
        for(long t : arr) if(t == value) return true;
        return false;
    }

    public static boolean allMatch(double[] arr, DoublePredicate filter) {
        for(double t : arr) if(!filter.test(t)) return false;
        return true;
    }

    public static boolean anyMatch(double[] arr, DoublePredicate filter) {
        for(double t : arr) if(filter.test(t)) return true;
        return false;
    }

    public static boolean allMatch(double[] arr, double value) {
        for(double t : arr) if(t != value) return false;
        return true;
    }

    public static boolean anyMatch(double[] arr, double value) {
        for(double t : arr) if(t == value) return true;
        return false;
    }


    /**
     * Returns whether the two collections are equal without considering the order of their elements.
     *
     * @param a The first collection
     * @param b The second collection
     * @return <code>true</code> iff both are null, or they have the same size and contain the same elements
     *         with the same number of occurrences, in an arbitrary order
     */
    public static boolean equalsIgnoreOrder(Collection<?> a, Collection<?> b) {
        if(a == b) return true;
        if(a == null || b == null) return false;

        int aSize = a.size(), bSize = b.size();
        if(aSize != bSize)
            return false;

        Map<Object, IntWrapper> counts = new HashMap<>(aSize);

        for(Object o : a)
            counts.computeIfAbsent(o, $ -> new IntWrapper()).value++;

        for(Object o : b) {
            IntWrapper count = counts.get(o);
            if(count == null || --count.value == -1) return false;
        }

        return true;
    }

    public static String toString(Collection<?> c) {
        if(c.isEmpty())
            return "[]";
        StringBuilder str = new StringBuilder("[");
        for(Object o : c)
            str.append(o).append(", ");
        str.setLength(str.length() - 2);
        return str.append(']').toString();
    }

    public static String toString(Map<?,?> m) {
        if(m.isEmpty())
            return "{}";
        StringBuilder str = new StringBuilder("{");
        m.forEach((k,v) -> str.append(k).append('=').append(v).append(", "));
        str.setLength(str.length() - 2);
        return str.append('}').toString();
    }

    public static boolean equals(List<?> list, Object obj) {
        if(obj == list) return true;
        if(!(obj instanceof List<?>))
            return false;
        int size = list.size();
        List<?> otherList = (List<?>) obj;
        if(otherList.size() != size)
            return false;
        for(int i=0; i<size; i++)
            if(!Objects.equals(list.get(i), otherList.get(i)))
                return false;
        return true;
    }

    public static boolean equals(Set<?> set, Object obj) {
        if(obj == set) return true;

        if (!(obj instanceof Set<?>)) return false;
        Set<?> otherSet = (Set<?>) obj;
        if(otherSet.size() != set.size())
            return false;
        try {
            //noinspection SuspiciousMethodCalls
            return set.containsAll(otherSet);
        } catch(ClassCastException | NullPointerException ignored) {
            return false;
        }
    }

    public static boolean equals(Map<?,?> map, Object obj) {
        if(obj == map) return true;

        if(!(obj instanceof Map)) return false;
        Map<?,?> otherMap = (Map<?,?>) obj;
        if(otherMap.size() != map.size()) return false;

        try {
            for(Map.Entry<?,?> e : map.entrySet()) {
                Object key = e.getKey();
                Object value = e.getValue();
                if(value == null) {
                    if(!(otherMap.get(key) == null && otherMap.containsKey(key)))
                        return false;
                }
                else {
                    if(!value.equals(otherMap.get(key)))
                        return false;
                }
            }
            return true;
        } catch(ClassCastException | NullPointerException ignored) {
            return false;
        }
    }

    public static int hashCode(List<?> list) {
        return hashCode(list, 0, list.size());
    }

    public static int hashCode(List<?> list, int from, int to) {
        int hashCode = 1;
        for(int i=from; i<to; i++)
            hashCode = 31 * hashCode + Objects.hashCode(list.get(i));
        return hashCode;
    }

    public static int hashCode(Set<?> set) {
        return Mathf.sum(set, Objects::hashCode);
    }

    public static int hashCode(Map<?,?> map) {
        return map.entrySet().hashCode();
    }



    public static String repeat(Object o, int n) {
        Arguments.checkRange(n, 0, null);
        String oStr = Objects.toString(o);
        StringBuilder str = new StringBuilder(oStr.length() * n);
        //noinspection StringRepeatCanBeUsed
        for(int i=0; i<n; i++) str.append(oStr);
        return str.toString();
    }

    public static String concatWithoutDuplicate(String a, String b) {
        for(int i=b.length(); i>0; i--) {
            String common = b.substring(0, i);
            if(a.endsWith(common))
                return a + b.substring(i);
        }
        return a + b;
    }

    private static final String[] BLANK = new String[200];

    public static String blank(int length) {
        if(Arguments.checkRange(length, 0, null) < BLANK.length) {
            if(BLANK[length] == null)
                BLANK[length] = repeat(" ", length);
            return BLANK[length];
        }
        return repeat(" ", length);
    }

    public static String flipCase(CharSequence str) {
        int len = str.length();
        char[] inv = new char[len];
        for(int i=0; i<len; i++) {
            char c = str.charAt(i);
            if(Character.isHighSurrogate(c) && i != len-1) {
                int pt = Character.toCodePoint(c, str.charAt(i+1));
                int invPt = Character.isLowerCase(pt) ? Character.toUpperCase(pt) : Character.toLowerCase(pt);
                inv[i] = Character.highSurrogate(pt);
                inv[++i] = Character.lowSurrogate(pt);
            }
            else inv[i] = Character.isLowerCase(c) ? Character.toUpperCase(c) : Character.toLowerCase(c);
        }
        return new String(inv);
    }

    public static String durationString(Duration duration) {
        return durationString(duration.toNanos());
    }

    public static String durationString(long nanos) {
        if(nanos == 0)
            return "0s";
        if(nanos < 0)
            return "-" + durationString(-nanos);

        long millis = nanos / 1000000;
        long fullSeconds = millis / 1000;
        long fullMinutes = fullSeconds / 60;
        long fullHours = fullMinutes / 60;
        long fullDays = fullHours / 24;
        double seconds = nanos / 1000000000.0;
        double years = fullHours / (24 * 356.2425);


        if(years >= 1)
            return String.format(Locale.ROOT, "%.3f years", years);
        if(fullDays >= 1)
            return String.format(Locale.ROOT, "%.3f days", fullSeconds / 86400.0);
        if(fullHours >= 1)
            return String.format(Locale.ROOT, "%d:%02d:%02d", fullHours, fullMinutes % 60, fullSeconds % 60);
        if(fullMinutes >= 1)
            return String.format(Locale.ROOT, "%02d:%05.2f", fullMinutes, seconds % 60);
        if(fullSeconds >= 1)
            return String.format(Locale.ROOT, "%.3fs", seconds);
        if(nanos >= 1000000)
            return String.format(Locale.ROOT, "%.3fms", nanos / 1000000d);
        return nanos+"ns";
    }


    public static String toBase64(String str) {
        return toBase64(str.getBytes());
    }

    public static String toBase64(byte[] bytes) {
        return Base64.getEncoder().encodeToString(bytes);
    }

    public static byte[] toBase64Bytes(String str) {
        return toBase64Bytes(str.getBytes());
    }

    public static byte[] toBase64Bytes(byte[] bytes) {
        return Base64.getEncoder().encode(bytes);
    }

    public static String fromBase64(String base64) {
        return new String(bytesFromBase64(base64));
    }

    public static byte[] bytesFromBase64(String base64) {
        return Base64.getDecoder().decode(base64);
    }

    public static String fromBase64(byte[] base64) {
        return new String(bytesFromBase64(base64));
    }

    public static byte[] bytesFromBase64(byte[] base64) {
        return Base64.getDecoder().decode(base64);
    }



    public static String getMessageWithCauses(Throwable t) {
        String msg = t.getMessage();
        Throwable cause = t.getCause();
        if(cause == null) return msg;

        String causeMsg = getMessageWithCauses(cause);
        if(causeMsg == null) return msg;
        if(msg == null) return causeMsg;

        return concatWithoutDuplicate(msg, ": "+causeMsg);
    }

    public static String getStackTraceString(Throwable t) {
        StringWriter str = new StringWriter();
        PrintWriter writer = new PrintWriter(str);
        t.printStackTrace(new PrintWriter(str));
        writer.close();
        return str.toString();
    }

    public static String[] getArgs() {
        List<String> args = new ArrayList<>();
        String cmd = System.getProperty("sun.java.command");
        StringBuilder str = new StringBuilder();

        char inString = 0;
        for(int i=0; i<cmd.length(); i++) {
            char c = cmd.charAt(i);
            if(inString != 0 && c == inString)
                inString = 0;
            else if(inString == 0 && (c == '\'' || c == '"'))
                inString = c;
            else if(inString == 0 && c == ' ') {
                args.add(str.toString());
                str.delete(0, str.length());
            }
            else str.append(c);
        }
        if(str.length() != 0) args.add(str.toString());

        if(args.isEmpty()) return new String[0];
        return args.subList(1, args.size()).toArray(new String[0]);
    }


    public static String readAll(InputStream in) {
        return readAll(in, StandardCharsets.UTF_8);
    }

    public static String readAll(InputStream in, Charset charset) {
        try(in) {
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            for(int length; (length = in.read(buffer)) != -1; )
                result.write(buffer, 0, length);
            return result.toString(charset);
        } catch(IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public static Future<Void> connect(InputStream in, OutputStream out) {
        Arguments.checkNull(in, "in");
        Arguments.checkNull(out, "out");
        FutureImpl<Void> result = new ThreadedFutureImpl<>();
        new Thread(() -> {
            try {
                in.transferTo(out);
                result.complete(null);
            } catch(Exception e) {
                result.fail(e);
            }
        }).start();
        return result;
    }


    public static void setField(Object clsOrInstance, String name, Object value) {
        Arguments.checkNull(clsOrInstance, "clsOrInstance");
        try {
            Class<?> type = clsOrInstance instanceof Class<?> ? (Class<?>) clsOrInstance : clsOrInstance.getClass();
            getField(type, name, true).set(clsOrInstance, value);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new UncheckedException(e);
        }
    }

    public static Object getField(Object clsOrInstance, String name) {
        Arguments.checkNull(clsOrInstance, "clsOrInstance");
        try {
            Class<?> type = clsOrInstance instanceof Class<?> ? (Class<?>) clsOrInstance : clsOrInstance.getClass();
            return getField(type, name, true).get(clsOrInstance);
        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new UncheckedException(e);
        }
    }

    public static Field getField(Class<?> type, String name, boolean setAccessible) {
        Arguments.checkNull(type, "type");
        Arguments.checkNull(name, "name");
        try {
            Field field = null;
            for(Class<?> t = type; t != null; t = t.getSuperclass()) {
                for(Field f : t.getDeclaredFields()) {
                    if(f.getName().equals(name)) {
                        field = f;
                        break;
                    }
                }
            }
            if(field == null) throw new NoSuchFieldException(type + "." + name);

            if(setAccessible)
                field.setAccessible(true);
            return field;

        } catch(RuntimeException e) {
            throw e;
        } catch(Exception e) {
            throw new UncheckedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T invoke(@NotNull Object clsOrInstance, @NotNull String methodName, Object @NotNull... params) {
        Arguments.checkNull(clsOrInstance, "clsOrInstance");
        Arguments.checkNull(methodName, "methodName");
        Arguments.checkNull(params, "params");

        Class<?> type = clsOrInstance instanceof Class<?> ? (Class<?>) clsOrInstance : clsOrInstance.getClass();
        Class<?>[] paramTypes = new Class[params.length];
        for(int i=0; i<params.length; i++)
            paramTypes[i] = params[i] != null ? params[i].getClass() : Object.class;

        try {
            Method method = null;
            outer: for(Class<?> t = type; t != null; t = t.getSuperclass()) {
                inner: for(Method m : t.getDeclaredMethods()) {
                    if(m.getName().equals(methodName) && m.getParameterCount() == params.length) {
                        Class<?>[] methodParams = m.getParameterTypes();
                        for(int i=0; i<params.length; i++)
                            if(!methodParams[i].isAssignableFrom(paramTypes[i]))
                                continue inner;
                        method = m;
                        break outer;
                    }
                }
            }
            if(method == null) throw new UncheckedException(new NoSuchMethodException(type + "." + methodName + " assignable with types " + Arrays.toString(paramTypes)));

            method.setAccessible(true);

            return (T) method.invoke(clsOrInstance, params);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if(cause instanceof RuntimeException)
                throw (RuntimeException) cause;
            if(cause instanceof Error)
                throw (Error) cause;
            throw new UncheckedException(cause);
        } catch (IllegalAccessException e) {
            throw new UncheckedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> MethodFunction<T> getMethod(@NotNull Object clsOrInstance, @NotNull String methodName, @NotNull Class<?> @NotNull... paramTypes) {
        Arguments.checkNull(clsOrInstance, "clsOrInstance");
        Class<?> type = clsOrInstance instanceof Class<?> ? (Class<?>) clsOrInstance : clsOrInstance.getClass();
        Method method = getMethodHandle(type, methodName, paramTypes);
        return (target, params) -> {
            try {
                return (T) method.invoke(target != null ? target : clsOrInstance, params);
            } catch (InvocationTargetException e) {
                Throwable cause = e.getCause();
                if(cause instanceof RuntimeException)
                    throw (RuntimeException) cause;
                if(cause instanceof Error)
                    throw (Error) cause;
                throw new UncheckedException(cause);
            } catch (IllegalAccessException e) {
                throw new UncheckedException(e);
            }
        };
    }

    public static Method getMethodHandle(@NotNull String className, @NotNull String methodName, @NotNull Class<?> @NotNull... paramTypes) {
        return getMethodHandle(getClass(className), methodName, paramTypes);
    }

    public static Method getMethodHandle(@NotNull Class<?> type, @NotNull String methodName, @NotNull Class<?> @NotNull... paramTypes) {
        return getMethodHandle(type, methodName, true, paramTypes);
    }

    public static Method getMethodHandle(@NotNull Class<?> type, @NotNull String methodName, boolean setAccessible, @NotNull Class<?> @NotNull... paramTypes) {
        Arguments.checkNull(type, "type");
        Arguments.checkNull(methodName, "methodName");
        Arguments.deepCheckNull(paramTypes, "paramTypes");
        try {
            Method method = null;
            outer: for(Class<?> t = type; t != null; t = t.getSuperclass()) {
                for(Method m : t.getDeclaredMethods()) {
                    if(m.getName().equals(methodName) && Arrays.equals(paramTypes, m.getParameterTypes())) {
                        method = m;
                        break outer;
                    }
                }
            }
            if(method == null) throw new NoSuchMethodException(type + "." + methodName);

            if(setAccessible)
                method.setAccessible(true);

            return method;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new UncheckedException(e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(@NotNull String className) {
        Arguments.checkNull(className, "className");
        try {
            return (Class<T>) Class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new UncheckedException(e);
        }
    }

    public static void deleteDirectory(Path directory) throws IOException {
        Files.walkFileTree(Arguments.checkNull(directory, "directory"), new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
                Files.delete(dir);
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
                Files.delete(file);
                return FileVisitResult.CONTINUE;
            }
        });
    }


    /**
     * Returns a platform-specific directory for app-data like files. The returned directory
     * is shared for all applications, thus a subdirectory should be chosen for actually storing
     * files.
     *
     * @return A directory for storing config files and similar
     */
    public static String getAppdata() {
        String os = System.getProperty("os.name").toLowerCase();
        if(os.contains("win"))
            return System.getenv("LOCALAPPDATA");
        String dir = System.getenv("XDG_DATA_HOME");
        return dir != null ? dir : System.getProperty("user.home") + "/.local/share";
    }



    /**
     * If the given exception is a {@link RuntimeException} or an {@link Error}, it throws it directly.
     * Otherwise, it throws an {@link UncheckedException} wrapping the given exception. If the supplied
     * exception is <code>null</code>, a {@link NullPointerException} will be thrown.
     * <p><b>This method never returns normally!</b></p>
     *
     * @param t The throwable to rethrow unchecked
     * @return Nothing. This method never returns normally. For convenience, the return type of this method
     *         is {@link RuntimeException} which allows to write <code>throw rethrow(...);</code>. This might
     *         be necessary in some cases for compilation reasons.
     */
    @Contract("_->fail")
    public static RuntimeException rethrow(Throwable t) throws RuntimeException, Error {
        return UncheckedException.rethrow(t);
    }



    private static class ListConcatenation<E> implements List<E> {

        private final List<? extends E> l1, l2;

        private ListConcatenation(List<? extends E> l1, List<? extends E> l2) {
            this.l1 = l1;
            this.l2 = l2;
        }

        @Override
        public int size() {
            return l1.size() + l2.size();
        }

        @Override
        public boolean isEmpty() {
            return l1.isEmpty() && l2.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return l1.contains(o) || l2.contains(o);
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return listIterator();
        }

        @NotNull
        @Override
        public Object @NotNull [] toArray() {
            return toArray(new Object[0]);
        }

        @SuppressWarnings("unchecked")
        @NotNull
        @Override
        public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
            int s1 = l1.size(), s2 = l2.size();
            if(a.length < s1 + s2)
                a = Arrays.copyOf(a, s1 + s2);
            int i = 0;
            for(E t : l1) a[i++] = (T) t;
            for(E t : l2) a[i++] = (T) t;
            return a;
        }

        @Override
        public boolean add(E e) {
            throw new ViewModificationException();
        }

        @Override
        public boolean remove(Object o) {
            throw new ViewModificationException();
        }

        @SuppressWarnings("SuspiciousMethodCalls")
        @Override
        public boolean containsAll(@NotNull Collection<?> c) {
            for(Object o : c) if(!l1.contains(o) && !l2.contains(o)) return false;
            return true;
        }

        @Override
        public boolean addAll(@NotNull Collection<? extends E> c) {
            throw new ViewModificationException();
        }

        @Override
        public boolean addAll(int index, @NotNull Collection<? extends E> c) {
            throw new ViewModificationException();
        }

        @Override
        public boolean removeAll(@NotNull Collection<?> c) {
            throw new ViewModificationException();
        }

        @Override
        public boolean retainAll(@NotNull Collection<?> c) {
            throw new ViewModificationException();
        }

        @Override
        public void clear() {
            throw new ViewModificationException();
        }

        @Override
        public E get(int index) {
            int s1 = l1.size();
            return index < s1 ? l1.get(index) : l2.get(index - s1);
        }

        @Override
        public E set(int index, E element) {
            throw new ViewModificationException();
        }

        @Override
        public void add(int index, E element) {
            throw new ViewModificationException();
        }

        @Override
        public E remove(int index) {
            throw new ViewModificationException();
        }

        @Override
        public int indexOf(Object o) {
            int index = l1.indexOf(o);
            if(index != -1) return index;
            index = l2.indexOf(o);
            if(index == -1) return -1;
            return index + l1.size();
        }

        @Override
        public int lastIndexOf(Object o) {
            int index = l2.lastIndexOf(o);
            if(index != -1) return l1.size() + index;
            return l1.lastIndexOf(o);
        }

        @NotNull
        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        @NotNull
        @Override
        public ListIterator<E> listIterator(int index) {
            return new It(index);
        }

        @NotNull
        @Override
        public List<E> subList(int fromIndex, int toIndex) {
            int s1 = l1.size();
            if(toIndex <= s1) return view(l1.subList(fromIndex, toIndex));
            if(fromIndex >= s1) return view(l2.subList(fromIndex - s1, toIndex - s1));
            return view(l1.subList(fromIndex, s1), l2.subList(0, toIndex - s1));
        }

        @SuppressWarnings("NewClassNamingConvention")
        private class It implements ListIterator<E> {

            private final ListIterator<? extends E> it1, it2;

            private It(int index) {
                int s1 = l1.size();
                if(index < s1) {
                    it1 = l1.listIterator(index);
                    it2 = l2.listIterator(0);
                }
                else {
                    it1 = l1.listIterator(s1);
                    it2 = l2.listIterator(index - s1);
                }
            }

            @Override
            public boolean hasNext() {
                return it1.hasNext() || it2.hasNext();
            }

            @Override
            public E next() {
                return it1.hasNext() ? it1.next() : it2.next();
            }

            @Override
            public boolean hasPrevious() {
                return it2.hasPrevious() || it1.hasPrevious();
            }

            @Override
            public E previous() {
                return it2.hasPrevious() ? it2.previous() : it1.previous();
            }

            @Override
            public int nextIndex() {
                return it1.nextIndex() + it2.nextIndex();
            }

            @Override
            public int previousIndex() {
                return it2.previousIndex() + it1.previousIndex() + 1;
            }

            @Override
            public void remove() {
                throw new ViewModificationException();
            }

            @Override
            public void set(E e) {
                throw new ViewModificationException();
            }

            @Override
            public void add(E e) {
                throw new ViewModificationException();
            }
        }

        @Override
        public boolean equals(Object o) {
            if(this == o) return true;
            if(!(o instanceof List)) return false;
            Iterator<?> it = ((List<?>) o).iterator();
            for(E e : this)
                if(!it.hasNext() || !Objects.equals(e, it.next())) return false;
            return !it.hasNext();
        }

        @Override
        public int hashCode() {
            int hashCode = 1;
            for (E e : this)
                hashCode = 31 * hashCode + (e == null ? 0 : e.hashCode());
            return hashCode;
        }

        @Override
        public String toString() {
            return "[" + stream().map(Objects::toString).collect(Collectors.joining(", ")) + "]";
        }
    }

    private static class ReverseIterator<T> implements Iterator<T> {

        private final ListIterator<T> it;

        private ReverseIterator(ListIterator<T> it) {
            this.it = it;
            while(it.hasNext()) it.next();
        }

        @Override
        public boolean hasNext() {
            return it.hasPrevious();
        }

        @Override
        public T next() {
            return it.previous();
        }

        @Override
        public void remove() {
            it.remove();
        }
    }

    private static final class MapFromSet<K,V> extends AbstractImmutableMap<K,V> {

        private final Set<? extends K> set;

        private MapFromSet(Set<? extends K> set) {
            this.set = Arguments.checkNull(set, "set");
        }

        @Override
        public String toString() {
            return entrySet().toString();
        }

        @Override
        public boolean equals(Object obj) {
            return obj == this || (obj instanceof Map && entrySet().equals(((Map<?, ?>) obj).entrySet()));
        }

        @Override
        public int hashCode() {
            return entrySet().hashCode();
        }

        @Override
        public int size() {
            return set.size();
        }

        @Override
        public boolean isEmpty() {
            return set.isEmpty();
        }

        @Override
        public boolean containsKey(Object key) {
            //noinspection SuspiciousMethodCalls
            return set.contains(key);
        }

        @Override
        public boolean containsValue(Object value) {
            return !set.isEmpty() && value == null;
        }

        @Override
        public V get(Object key) {
            return null;
        }

        @NotNull
        @Override
        public Set<K> keySet() {
            return Utils.view(set);
        }

        @NotNull
        @Override
        public Collection<V> values() {
            return new Values();
        }

        @NotNull
        @Override
        public Set<Entry<K, V>> entrySet() {
            return new EntrySet();
        }

        @Override
        public void forEach(BiConsumer<? super K, ? super V> action) {
            set.forEach(k -> action.accept(k, null));
        }

        private final class Values extends AbstractImmutableCollection<V> {

            @Override
            public int size() {
                return set.size();
            }

            @Override
            public boolean isEmpty() {
                return set.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return !set.isEmpty() && o == null;
            }

            @NotNull
            @Override
            public Iterator<V> iterator() {
                return Stream.<V>iterate(null, $->null).limit(set.size()).iterator();
            }

            @Override
            public <T> T @NotNull [] toArray(T @NotNull [] a) {
                if(a.length < set.size())
                    a = Arrays.copyOf(a, set.size());
                Arrays.fill(a, null);
                return a;
            }

            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                if(set.isEmpty()) return c.isEmpty();
                return c.stream().allMatch(Objects::isNull);
            }
        }

        private final class EntrySet extends AbstractImmutableSet<Entry<K,V>> {

            @Override
            public int size() {
                return set.size();
            }

            @Override
            public boolean isEmpty() {
                return set.isEmpty();
            }

            @Override
            public boolean contains(Object o) {
                return !set.isEmpty() && o instanceof Entry && ((Entry<?, ?>) o).getValue() == null && set.contains(((Entry<?, ?>) o).getKey());
            }

            @NotNull
            @Override
            public Iterator<Entry<K, V>> iterator() {
                return new MappingIterator<>(set, k -> entry(k,null));
            }

            @SuppressWarnings("unchecked")
            @Override
            public <T> T @NotNull [] toArray(T @NotNull [] a) {
                if(a.length < set.size())
                    a = Arrays.copyOf(a, set.size());
                int i = 0;
                for(K k : set)
                    a[i++] = (T) entry(k, null);
                return a;
            }

            @Override
            public boolean containsAll(@NotNull Collection<?> c) {
                if(set.isEmpty()) return c.isEmpty();
                return c.stream().allMatch(this::contains);
            }
        }
    }

    public static <K,V> Map.Entry<K,V> entry(K k, V v) {
        return new Map.Entry<>() {

            @Override
            public String toString() {
                return k+" -> "+v;
            }

            @Override
            public boolean equals(Object obj) {
                return obj instanceof Map.Entry &&
                        Objects.equals(k, ((Map.Entry<?,?>) obj).getKey()) &&
                        Objects.equals(v, ((Map.Entry<?,?>) obj).getValue());
            }

            @Override
            public int hashCode() {
                return (k==null ? 0 : k.hashCode()) ^ (v==null ? 0 : v.hashCode());
            }

            @Override
            public K getKey() {
                return k;
            }
            @Override
            public V getValue() {
                return v;
            }

            @Override
            public V setValue(V value) {
                throw new ImmutabilityException(this);
            }
        };
    }
}
