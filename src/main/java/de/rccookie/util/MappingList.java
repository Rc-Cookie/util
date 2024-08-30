//package de.rccookie.util;
//
//import java.util.Arrays;
//import java.util.Collection;
//import java.util.Iterator;
//import java.util.List;
//import java.util.ListIterator;
//import java.util.Objects;
//import java.util.function.Function;
//import java.util.stream.Collectors;
//
//import org.jetbrains.annotations.NotNull;
//
//public class MappingList<I,O> implements List<O> {
//
//    private final List<I> list;
//    private final Function<? super I, ? extends O> mapping;
//    private final Function<? super O, ? extends I> reverse;
//
//    public MappingList(List<I> list, Function<? super I, ? extends O> mapping, Function<? super O, ? extends I> reverse) {
//        this.list = Arguments.checkNull(list, "list");
//        this.mapping = Arguments.checkNull(mapping, "mapping");
//        this.reverse = reverse;
//    }
//
//    @Override
//    public String toString() {
//        return "["+list.stream().map(o -> Objects.toString(mapping.apply(o))).collect(Collectors.joining(", "))+"]";
//    }
//
//    @Override
//    public boolean equals(Object obj) {
//        if(obj == this) return true;
//        if(!(obj instanceof List)) return false;
//        List<?> l = (List<?>) obj;
//        if(l.size() != size()) return false;
//        Iterator<?> it = l.iterator();
//        for(I o : list)
//            if(!Objects.equals(mapping.apply(o), it.next())) return false;
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int hashCode = 1;
//        for(I o : list)
//            hashCode = 31 * hashCode + Objects.hashCode(mapping.apply(o));
//        return hashCode;
//    }
//
//    @Override
//    public int size() {
//        return list.size();
//    }
//
//    @Override
//    public boolean isEmpty() {
//        return list.isEmpty();
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean contains(Object o) {
//        if(reverse != null) try {
//            return list.contains(reverse.apply((O) o));
//        } catch(ClassCastException ignored) { }
//        return list.stream().anyMatch(e -> Objects.equals(e,o));
//    }
//
//    @NotNull
//    @Override
//    public Iterator<O> iterator() {
//        return new MappingIterator<>(list, mapping);
//    }
//
//    @SuppressWarnings("unchecked")
//    @NotNull
//    @Override
//    public Object[] toArray() {
//        Object[] arr = list.toArray();
//        Arrays.setAll(arr, i -> mapping.apply((I) arr[i]));
//        return arr;
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public <U> U[] toArray(U @NotNull [] a) {
//        Object[] arr = list.toArray();
//        if(a.length < arr.length)
//            a = Arrays.copyOf(a, arr.length);
//        Arrays.setAll(a, i -> mapping.apply((I) arr[i]));
//        return a;
//    }
//
//    @Override
//    public boolean add(O t) {
//        if(reverse == null)
//            throw new UnsupportedOperationException();
//        return list.add(reverse.apply(t));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean remove(Object o) {
//        if(reverse != null) try {
//            return list.remove(reverse.apply((O) o));
//        } catch(ClassCastException ignored) { }
//        return list.remove(list.get(indexOf(o)));
//    }
//
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean containsAll(@NotNull Collection<?> c) {
//        try {
//            //noinspection SlowListContainsAll
//            return list.containsAll(new MappingSetView<>((Collection<? extends O>) c, reverse, mapping));
//        } catch(ClassCastException e) {
//            return list.stream().map(mapping).collect(Collectors.toSet()).containsAll(c);
//        }
//    }
//
//    @Override
//    public boolean addAll(@NotNull Collection<? extends O> c) {
//        if(reverse == null)
//            throw new UnsupportedOperationException();
//        return list.addAll(
//    }
//
//    @Override
//    public boolean addAll(int index, @NotNull Collection<? extends O> c) {
//        return false;
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
//
//    @Override
//    public O get(int index) {
//        return null;
//    }
//
//    @Override
//    public O set(int index, O element) {
//        return null;
//    }
//
//    @Override
//    public void add(int index, O element) {
//
//    }
//
//    @Override
//    public O remove(int index) {
//        return null;
//    }
//
//    @Override
//    public int indexOf(Object o) {
//        return 0;
//    }
//
//    @Override
//    public int lastIndexOf(Object o) {
//        return 0;
//    }
//
//    @NotNull
//    @Override
//    public ListIterator<O> listIterator() {
//        return null;
//    }
//
//    @NotNull
//    @Override
//    public ListIterator<O> listIterator(int index) {
//        return null;
//    }
//
//    @NotNull
//    @Override
//    public List<O> subList(int fromIndex, int toIndex) {
//        return null;
//    }
//}
