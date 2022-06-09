package com.github.rccookie.util;

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
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.stream.Stream;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.Nullable;

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
                return array.length < i;
            }

            @Override
            public T next() {
                if(!hasNext())
                    throw new EmptyIteratorException();
                return array[i++];
            }
        };
    }



    public static <T> Iterator<T> emptyIterator() {
        //noinspection unchecked
        return (Iterator<T>) EMPTY_ITERATOR;
    }

    public static <T> Iterable<T> iterate(Stream<T> stream) {
        return stream::iterator;
    }

    public static <T> Stream<T> stream(Iterator<T> iterator) {
        if(!iterator.hasNext()) return Stream.empty();
        return Stream.iterate(iterator.next(), $ -> iterator.hasNext(), $ -> iterator.next());
    }

    public static <T> Stream<T> filterType(Stream<?> stream, Class<T> type) {
        return stream.filter(o -> o == null || type.isInstance(o)).map(type::cast);
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



    public static String repeat(Object o, int n) {
        Arguments.checkRange(n, 0, null);
        String oStr = Objects.toString(o);
        StringBuilder str = new StringBuilder(oStr.length() * n);
        //noinspection StringRepeatCanBeUsed
        for(int i=0; i<n; i++) str.append(oStr);
        return str.toString();
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
}
