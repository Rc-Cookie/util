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
//public class MappingSetView<I,O> extends ImmutableSet<O> {
//
//    private final Collection<? extends I> data;
//    private final Function<? super I, ? extends O> mapping;
//    private final Function<? super O, ? extends I> reverse;
//
//    public MappingSetView(Collection<? extends I> data, Function<? super I, ? extends O> mapping, @Nullable Function<? super O, ? extends I> reverse) {
//        this.data = Arguments.checkNull(data, "data");
//        this.mapping = Arguments.checkNull(mapping, "mapping");
//        this.reverse = reverse;
//    }
//
//    public MappingSetView(Collection<? extends I> data, Function<? super I, ? extends O> mapping) {
//        this(data, mapping, null);
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
//        return new MappingIterator<>(data.iterator(), mapping);
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
//    @SuppressWarnings("unchecked")
//    @Override
//    public boolean containsAll(@NotNull Collection<?> c) {
//        try {
//            return data.containsAll(new MappingSetView<>((Collection<? extends O>) c, reverse, mapping));
//        } catch(ClassCastException e) {
//            return data.stream().map(mapping).collect(Collectors.toSet()).containsAll(c);
//        }
//    }
//}
