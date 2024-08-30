//package de.rccookie.util;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.Objects;
//import java.util.Set;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import org.jetbrains.annotations.NotNull;
//
//public class MappingSet<I,O> implements Set<O> {
//
//    private final Collection<I> data;
//    private final Function<I,O> mapping;
//    private final Function<O,I> reverse;
//
//    public MappingSet(Collection<I> data, Function<I, O> mapping, Function<O, I> reverse) {
//        this.data = Arguments.checkNull(data, "set");
//        this.mapping = mapping;
//        this.reverse = reverse;
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
//        try {
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
//    public boolean add(O e) {
//        return data.add(reverse.apply(e));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean remove(Object o) {
//        try {
//            return data.remove(reverse.apply((O) o));
//        } catch(ClassCastException ignored) { }
//        return data.stream().filter(e -> Objects.equals(e,o)).findAny().map(data::remove).orElse(false);
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean containsAll(@NotNull Collection<?> c) {
//        try {
//            return data.containsAll(new MappingSetView<>((Collection<? extends O>) c, reverse, mapping));
//        } catch(ClassCastException e) {
//            return data.stream().map(mapping).collect(Collectors.toSet()).containsAll(c);
//        }
//    }
//
//    @Override
//    public boolean addAll(@NotNull Collection<? extends O> c) {
//        return data.addAll(new MappingSetView<>(c, reverse, mapping));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean retainAll(@NotNull Collection<?> c) {
//        try {
//            return data.retainAll(new MappingSetView<>((Collection<? extends O>) c, reverse, mapping));
//        } catch(ClassCastException e) {
//            return data.removeIf(o -> !c.contains(o));
//        }
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean removeAll(@NotNull Collection<?> c) {
//        try {
//            return data.removeAll(new MappingSetView<>((Collection<? extends O>) c, reverse, mapping));
//        } catch(ClassCastException e) {
//            return data.removeIf(c::contains);
//        }
//    }
//
//    @Override
//    public void clear() {
//        data.clear();
//    }
//}
