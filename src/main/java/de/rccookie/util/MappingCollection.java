//package de.rccookie.util;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.Objects;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//public class MappingCollection<I,O> implements Collection<O> {
//
//    final Collection<I> data;
//    final Function<? super I, ? extends O> mapping;
//    @Nullable
//    final Function<? super O, ? extends I> reverse;
//    final boolean readonly;
//
//    public MappingCollection(Collection<I> data, @NotNull Function<? super I, ? extends O> mapping, @Nullable Function<? super O, ? extends I> reverse, boolean readonly) {
//        this.data = Arguments.checkNull(data, "data");
//        this.mapping = Arguments.checkNull(mapping, "mapping");
//        this.reverse = reverse;
//        this.readonly = readonly;
//    }
//
//    @Override
//    public String toString() {
//        return "["+data.stream().map(o -> Objects.toString(mapping.apply(o))).collect(Collectors.joining(", "))+"]";
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if(obj == this) return true;
//        if(!(obj instanceof Collection)) return false;
//        Collection<?> col = (Collection<?>) obj;
//        if(size() != col.size()) return false;
//        return data.stream().map(mapping).collect(Collectors.toList()).equals(obj);
//    }
//
//    @Override
//    public int hashCode() {
//        return data.stream().map(mapping).collect(Collectors.toList()).hashCode();
//    }
//
//    @Override
//    public int size() {
//        return data.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return data.isEmpty();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean contains(Object o) {
//        if(reverse != null) try {
//            return data.contains(reverse.apply((O) o));
//        } catch(ClassCastException ignored) { }
//        return data.stream().anyMatch(e -> Objects.equals(e,o));
//    }
//
//    @NotNull
//    @Override
//    public Iterator<O> iterator() {
//        return new MappingIterator<>(data, mapping);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public Object[] toArray() {
//        Object[] arr = data.toArray();
//        Arrays.setAll(arr, i -> mapping.apply((I) arr[i]));
//        return arr;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <T> T[] toArray(T @NotNull [] a) {
//        Object[] arr = data.toArray();
//        if(a.length < arr.length)
//            a = Arrays.copyOf(a, arr.length);
//        Arrays.setAll(a, i -> mapping.apply((I) arr[i]));
//        return a;
//    }
//
//    @Override
//    public boolean add(O o) {
//        if(reverse == null)
//            throw new UnsupportedOperationException();
//        return data.add(reverse.apply(o));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean remove(Object o) {
//        if(reverse != null) try {
//            return data.remove(reverse.apply((O) o));
//        } catch(ClassCastException ignored) { }
//        return data.stream().filter(e -> Objects.equals(e,o)).findAny().map(data::remove).orElse(false);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean containsAll(@NotNull Collection<?> c) {
//        try {
//            return data.containsAll(new MappingCollection<>((Collection<? extends O>) c, reverse, mapping, readonly));
//        } catch(ClassCastException e) {
//            return data.stream().map(mapping).collect(Collectors.toSet()).containsAll(c);
//        }
//    }
//
//    @Override
//    public boolean addAll(@NotNull Collection<? extends O> c) {
//        return data.addAll(new MappingCollection<>(c, reverse, mapping, readonly));
//    }
//
//    @Override
//    public boolean removeAll(@NotNull Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public boolean retainAll(@NotNull Collection<?> c) {
//        return false;
//    }
//
//    @Override
//    public void clear() {
//
//    }
//}
