package de.rccookie.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.jetbrains.annotations.NotNull;

/**
 * Collection of classes for lazy-loading values and collections.
 */
public final class Lazy<T> {

    private Supplier<? extends T> supplier;
    private T value;

    private Lazy(Supplier<? extends T> supplier) {
        this.supplier = Arguments.checkNull(supplier, "supplier");
    }

    private Lazy(T value) {
        this.value = value;
    }

    /**
     * Returns the value of this lazy. If not yet done it will first be computed
     *
     * @return The value of this lazy
     */
    public synchronized T value() {
        if(supplier != null) {
            value = supplier.get();
            supplier = null;
        }
        return value;
    }

    /**
     * Returns whether the underlying value has already been computed.
     *
     * @return Whether the value is already present
     */
    public boolean isComputed() {
        return supplier == null;
    }


    /**
     * Returns a new wrapped which will load its value on demand using the given supplier.
     *
     * @param supplier The supplier to be used to obtain the actual value, when requested
     * @return A Lazy object with the value returned by the given supplier
     * @param <T> The content type of the Lazy object
     */
    public static <T> Lazy<T> of(Supplier<? extends T> supplier) {
        return new Lazy<>(supplier);
    }

    /**
     * Returns a Lazy object whose value is already computed.
     *
     * @param t The value for the Lazy object
     * @return A Lazy object with the given value
     * @param <T> The type of the value
     */
    public static <T> Lazy<T> value(T t) {
        return new Lazy<>(t);
    }

    /**
     * Returns a lazily populated list with the contents returned by the given iterator.
     * The returned list is read-only.
     *
     * @param src The iterator returning the items for the list
     * @return A lazily populated list containing all elements returned by the iterator
     * @param <E> The content type of the list
     */
    public static <E> java.util.List<E> list(Iterator<? extends E> src) {
        return new List<>(src);
    }

    /**
     * Returns a lazily populated set with the contents returned by the given iterator.
     * The returned set is read-only. If the iterator returned duplicate object, there is
     * no guaranty made over which object references will be chosen to be within the set.
     *
     * @param src The iterator returning the items for the set
     * @return A lazily populated set containing all distinct elements returned by the iterator
     * @param <E> The content type of the set
     */
    public static <E>java.util.Set<E> set(Iterator<? extends E> src) {
        return new Set<>(src);
    }

    /**
     * Returns a lazily populated collection with the contents returned by the given iterator.
     * The returned collection is read-only.
     *
     * @param src The iterator returning the items for the collection
     * @return A lazily populated collection containing all elements returned by the iterator
     * @param <E> The content type of the collection
     */
    public static <E> java.util.Collection<E> collection(Iterator<? extends E> src) {
        return new Collection<>(src);
    }

    /**
     * Returns a lazily populated map with the contents returned by the given iterator.
     * The returned map is read-only. If the iterator returns the same key multiple times, there
     * is no guaranty made to which of the values will be within the returned map.
     *
     * @param src The iterator returning the items for the map
     * @return A lazily populated map containing all distinct entries returned by the iterator
     * @param <K> The key type of the map
     * @param <V> The value type of the map
     */
    public static <K,V> java.util.Map<K,V> map(Iterator<? extends java.util.Map.Entry<? extends K, ? extends V>> src) {
        return new Map<>(src);
    }

    /**
     * Returns a lazily populated map with the contents returned by the given iterator, where the
     * order in which the objects will be reported on iteration is consistent with the order in
     * which the iterator returned them. The returned map is read-only. If the iterator returns
     * the same key multiple times, there is no guaranty made to which of the values will be within
     * the returned map.
     *
     * @param src The iterator returning the items for the map
     * @return A lazily populated map containing all distinct entries returned by the iterator
     * @param <K> The key type of the map
     * @param <V> The value type of the map
     */
    public static <K,V> java.util.Map<K,V> orderedMap(Iterator<? extends java.util.Map.Entry<? extends K, ? extends V>> src) {
        return new Map<>(src);
    }



    private static final class List<E> extends AbstractImmutableList<E> {

        private final Iterator<? extends E> src;
        private final java.util.List<E> data = new ArrayList<>();

        private List(Iterator<? extends E> src) {
            this.src = Arguments.checkNull(src, "src");
        }

        private java.util.List<E> loadAll() {
            return load(Integer.MAX_VALUE);
        }

        private java.util.List<E> load(int toIndex) {
            while(data.size() <= toIndex && src.hasNext())
                data.add(src.next());
            return data;
        }

        private boolean loadNext() {
            if(!src.hasNext()) return false;
            data.add(src.next());
            return true;
        }

        @Override
        public String toString() {
            return loadAll().toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this) return true;
            if(!(obj instanceof java.util.List)) return false;
            java.util.List<?> list = (java.util.List<?>) obj;
            if(data.size() > list.size() || (data.size() == list.size() && src.hasNext())) return false;
            int i = 0;
            for(; i<data.size(); i++)
                if(!Objects.equals(data.get(i), list.get(i))) return false;
            for(; i<list.size() && loadNext(); i++)
                if(!Objects.equals(data.get(i), list.get(i))) return false;
            return !src.hasNext();
        }

        @Override
        public int hashCode() {
            return loadAll().hashCode();
        }

        @Override
        public int size() {
            return loadAll().size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty() && !src.hasNext();
        }

        @Override
        public boolean contains(Object o) {
            if(data.contains(o)) return true;
            while(loadNext())
                if(Objects.equals(o, data.get(data.size()-1)))
                    return true;
            return false;
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return listIterator();
        }

        @NotNull
        @Override
        public Object @NotNull [] toArray() {
            return loadAll().toArray();        }

        @NotNull
        @Override
        public <T> T @NotNull [] toArray(T@NotNull[] a) {
            return loadAll().toArray(a);
        }

        @Override
        public boolean containsAll(@NotNull java.util.Collection<?> c) {
            for(Object o : c)
                if(!contains(o)) return false;
            return true;
        }

        @Override
        public E get(int index) {
            load(index);
            return data.get(index);
        }

        @Override
        public int indexOf(Object o) {
            int index = data.indexOf(o);
            if(index != -1) return index;
            for(int i=data.size(); src.hasNext(); i++) {
                E e = src.next();
                data.add(e);
                if(Objects.equals(o,e)) return i;
            }
            return -1;
        }

        @Override
        public int lastIndexOf(Object o) {
            return loadAll().lastIndexOf(o);
        }

        @NotNull
        @Override
        public ListIterator<E> listIterator() {
            return listIterator(0);
        }

        @NotNull
        @Override
        public ListIterator<E> listIterator(int index) {
            if(index < 0) throw new IndexOutOfBoundsException(index);
            if(load(index-1).size() < index) throw new IndexOutOfBoundsException(index);
            return new ListIterator<>() {
                int i = index;
                @Override
                public boolean hasNext() {
                    return load(i-1).size() >= i || src.hasNext();
                }

                @Override
                public E next() {
                    return get(i++);
                }

                @Override
                public boolean hasPrevious() {
                    return i > 0;
                }

                @Override
                public E previous() {
                    return get(--i);
                }

                @Override
                public int nextIndex() {
                    return i;
                }

                @Override
                public int previousIndex() {
                    return i-1;
                }

                @Override
                public void remove() {
                    throw new ImmutabilityException();
                }

                @Override
                public void set(E e) {
                    throw new ImmutabilityException();
                }

                @Override
                public void add(E e) {
                    throw new ImmutabilityException();
                }
            };
        }

        @NotNull
        @Override
        public java.util.List<E> subList(int fromIndex, int toIndex) {
            return Utils.view(load(toIndex).subList(fromIndex, toIndex));
        }

        @Override
        public UnsupportedOperationException newException() {
            return new ImmutabilityException();
        }
    }

    private static final class Set<E> extends AbstractImmutableSet<E> {

        private final Iterator<? extends E> src;
        private final java.util.Set<E> data = new LinkedHashSet<>();

        private Set(Iterator<? extends E> src) {
            this.src = Arguments.checkNull(src, "src");
        }

        private java.util.Set<E> loadAll() {
            while(src.hasNext())
                data.add(src.next());
            return data;
        }

        @Override
        public String toString() {
            return loadAll().toString();
        }

        @Override
        public boolean equals(Object obj) {
            if(obj == this) return true;
            if(!(obj instanceof java.util.Set)) return false;
            java.util.Set<?> set = (java.util.Set<?>) obj;
            if(!src.hasNext()) return data.equals(set);
            if(data.size() > set.size()) return false;
            //noinspection SuspiciousMethodCalls
            if(!set.containsAll(data)) return false;
            while(data.size() < set.size() && src.hasNext()) {
                E e = src.next();
                if(data.add(e) && !set.contains(e)) return false;
            }
            if(data.size() != set.size()) return false;
            while(src.hasNext())
                if(data.add(src.next())) return false;
            //noinspection SuspiciousMethodCalls
            return data.containsAll(set);
        }

        @Override
        public int hashCode() {
            return loadAll().hashCode();
        }

        @Override
        public int size() {
            return loadAll().size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty() && !src.hasNext();
        }

        @Override
        public boolean contains(Object o) {
            if(data.contains(o)) return true;
            while(src.hasNext()) {
                E e = src.next();
                data.add(e);
                if(Objects.equals(o,e)) return true;
            }
            return false;
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return new OrderedCollectionIterator<E,E>(src, data::add, e->e, data);
        }

        @NotNull
        @Override
        public Object @NotNull [] toArray() {
            return loadAll().toArray();
        }

        @NotNull
        @Override
        public <T> T @NotNull [] toArray(T @NotNull[] a) {
            return loadAll().toArray(a);
        }

        @Override
        public boolean containsAll(@NotNull java.util.Collection<?> c) {
            for(Object o : c)
                if(!contains(o)) return false;
            return true;
        }

        @Override
        public UnsupportedOperationException newException() {
            return new ImmutabilityException();
        }
    }

    private static final class Collection<E> extends AbstractImmutableCollection<E> {

        private final Iterator<? extends E> src;
        private final java.util.List<E> data = new ArrayList<>();

        private Collection(Iterator<? extends E> src) {
            this.src = Arguments.checkNull(src, "src");
        }

        private java.util.List<E> loadAll() {
            while(src.hasNext())
                data.add(src.next());
            return data;
        }

        private boolean loadNext() {
            if(!src.hasNext()) return false;
            data.add(src.next());
            return true;
        }

        @Override
        public String toString() {
            return loadAll().toString();
        }

        @Override
        public int size() {
            return loadAll().size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty() && !src.hasNext();
        }

        @Override
        public boolean contains(Object o) {
            if(data.contains(o)) return true;
            while(loadNext())
                if(Objects.equals(o, data.get(data.size()-1)))
                    return true;
            return false;
        }

        @NotNull
        @Override
        public Iterator<E> iterator() {
            return new Iterator<>() {
                int i = 0;
                @Override
                public boolean hasNext() {
                    return i < data.size() || src.hasNext();
                }

                @Override
                public E next() {
                    if(!hasNext()) throw new EmptyIteratorException();
                    if(i == data.size())
                        data.add(src.next());
                    return data.get(i++);
                }
            };
        }

        @NotNull
        @Override
        public Object @NotNull [] toArray() {
            return loadAll().toArray();
        }

        @Override
        public <T> T @NotNull [] toArray(T@NotNull[] a) {
            return loadAll().toArray(a);
        }

        @Override
        public boolean containsAll(@NotNull java.util.Collection<?> c) {
            for(Object o : c)
                if(!contains(o)) return false;
            return true;
        }

        @Override
        public UnsupportedOperationException newException() {
            return new ImmutabilityException();
        }
    }

    private static final class Map<K,V> extends AbstractImmutableMap<K,V> {

        private final Iterator<? extends Entry<? extends K, ? extends V>> src;
        private final java.util.Map<K,V> data = new LinkedHashMap<>();
        private java.util.Set<K> keySet = null;
        private java.util.Collection<V> values = null;
        private java.util.Set<Entry<K,V>> entrySet = null;

        private Map(Iterator<? extends Entry<? extends K, ? extends V>> src) {
            this.src = Arguments.checkNull(src, "src");
        }

        private java.util.Map<K,V> loadAll() {
            while(src.hasNext()) {
                Entry<? extends K, ? extends V> entry = src.next();
                data.put(entry.getKey(), entry.getValue());
            }
            return data;
        }

        @Override
        public String toString() {
            return loadAll().toString();
        }

        @Override
        public int hashCode() {
            return loadAll().hashCode();
        }

        @Override
        public int size() {
            return loadAll().size();
        }

        @Override
        public boolean isEmpty() {
            return data.isEmpty() && !src.hasNext();
        }

        @Override
        public boolean containsKey(Object key) {
            if(data.containsKey(key)) return true;
            while(src.hasNext()) {
                Entry<? extends K, ? extends V> entry = src.next();
                if(data.put(entry.getKey(), entry.getValue()) == null && Objects.equals(key, entry.getKey()))
                    return true;
            }
            return false;
        }

        @Override
        public boolean containsValue(Object value) {
            if(data.containsValue(value)) return true;
            while(src.hasNext()) {
                Entry<? extends K, ? extends V> entry = src.next();
                data.put(entry.getKey(), entry.getValue());
                if(Objects.equals(value, entry.getValue()))
                    return true;
            }
            return false;
        }

        @Override
        public V get(Object key) {
            //noinspection SuspiciousMethodCalls
            if(!src.hasNext() || data.containsKey(key))
                return data.get(key);
            while(src.hasNext()) {
                Entry<? extends K, ? extends V> entry = src.next();
                if(data.put(entry.getKey(), entry.getValue()) == null && Objects.equals(key, entry.getKey()))
                    return entry.getValue();
            }
            return null;
        }

        @NotNull
        @Override
        public java.util.Set<K> keySet() {
            if(keySet != null) return keySet;
            return keySet = new AbstractImmutableSet<>() {
                @Override
                public int size() {
                    return Lazy.Map.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return Lazy.Map.this.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    return containsKey(o);
                }

                @NotNull
                @Override
                public Iterator<K> iterator() {
                    return new OrderedCollectionIterator<>(src, e -> {
                        int initSize = data.size();
                        return data.put(e.getKey(), e.getValue()) == null && data.size() != initSize;
                    }, Entry::getKey, data.keySet());
                }

                @NotNull
                @Override
                public Object @NotNull [] toArray() {
                    return loadAll().keySet().toArray();
                }

                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    return loadAll().keySet().toArray(a);
                }

                @Override
                public boolean containsAll(@NotNull java.util.Collection<?> c) {
                    for(Object o : c)
                        if(!containsKey(o)) return false;
                    return true;
                }
            };
        }

        @NotNull
        @Override
        public java.util.Collection<V> values() {
            if(values != null) return values;
            return values = new AbstractImmutableCollection<>() {
                @Override
                public int size() {
                    return Map.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return Map.this.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    return containsValue(o);
                }

                @NotNull
                @Override
                public Iterator<V> iterator() {
                    return new OrderedCollectionIterator<>(src, e -> {
                        int initSize = data.size();
                        return data.put(e.getKey(), e.getValue()) == null && data.size() != initSize;
                    }, Entry::getValue, data.values());
                }

                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    return loadAll().values().toArray(a);
                }

                @Override
                public boolean containsAll(@NotNull java.util.Collection<?> c) {
                    for(Object o : c)
                        if(!containsValue(o)) return false;
                    return true;
                }
            };
        }

        @NotNull
        @Override
        public java.util.Set<Entry<K,V>> entrySet() {
            if(entrySet != null) return entrySet;
            return entrySet = new AbstractImmutableSet<>() {
                @Override
                public int size() {
                    return Lazy.Map.this.size();
                }

                @Override
                public boolean isEmpty() {
                    return Lazy.Map.this.isEmpty();
                }

                @Override
                public boolean contains(Object o) {
                    if(!(o instanceof Entry)) return false;
                    Entry<?, ?> entry = (Entry<?, ?>) o;
                    Object k = entry.getKey(), v = entry.getValue();
                    return (v != null || containsKey(k)) && Objects.equals(get(k), v);
                }

                @NotNull
                @Override
                public Iterator<Entry<K, V>> iterator() {
                    return new OrderedCollectionIterator<>(src, e -> {
                        int initSize = data.size();
                        return data.put(e.getKey(), e.getValue()) == null && data.size() != initSize;
                    }, e -> java.util.Map.entry(e.getKey(), e.getValue()), data.entrySet());
                }

                @NotNull
                @Override
                public <T> T @NotNull [] toArray(@NotNull T @NotNull [] a) {
                    return loadAll().entrySet().toArray(a);
                }

                @Override
                public boolean containsAll(@NotNull java.util.Collection<?> c) {
                    for (Object o : c)
                        if(!contains(o)) return false;
                    return true;
                }
            };
        }

        @Override
        public UnsupportedOperationException newException() {
            return new ImmutabilityException();
        }
    }


    private static final class OrderedCollectionIterator<T,E> implements Iterator<E> {

        private final java.util.Collection<? extends E> data;
        private final Iterator<? extends T> src;
        private final Predicate<? super T> add;
        private final Function<? super T, ? extends E> extractor;
        private int i = 0;
        private int expectedSize;
        private int hasNext = 0;
        private E next;
        private Iterator<? extends E> it;

        private OrderedCollectionIterator(Iterator<? extends T> src, Predicate<? super T> add, Function<? super T, ? extends E> extractor, java.util.Collection<? extends E> data) {
            this.data = Arguments.checkNull(data, "data");
            this.src = Arguments.checkNull(src, "src");
            this.add = Arguments.checkNull(add, "add");
            this.extractor = Arguments.checkNull(extractor, "extractor");
            expectedSize = data.size();
            it = data.iterator();
        }

        void checkState() {
            if(expectedSize < data.size()) {
                // data has been fetched concurrently, get a new iterator and skip the first i elements because the old
                // one will probably throw a ConcurrentModificationException on use
                it = data.iterator();
                for(int j=0; j<i; j++) it.next();
                expectedSize = data.size();
            }
        }

        @Override
        public boolean hasNext() {
            if(hasNext != 0) return hasNext > 0;
            checkState();
            i++;
            if(it != null) {
                next = it.next();
                hasNext = 1;
                if(!it.hasNext()) it = null; // Remove reference to prevent ConcurrentModificationException on later hasNext() calls
                return true;
            }
            while(src.hasNext()) {
                T entry = src.next();
                if(add.test(entry)) { // Key is actually new
                    expectedSize++;
                    next = extractor.apply(entry);
                    hasNext = 1;
                    return true;
                }
            }
            return false;
        }

        @Override
        public E next() {
            if(!hasNext()) throw new EmptyIteratorException();
            hasNext = 0;
            return next;
        }
    }
}
