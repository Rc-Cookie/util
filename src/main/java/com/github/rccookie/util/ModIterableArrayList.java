package com.github.rccookie.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

import org.jetbrains.annotations.NotNull;

/**
 * Subclass of {@link ArrayList} that returns an {@link ModIterator}
 * as iterator.
 */
public class ModIterableArrayList<E> extends ArrayList<E> {

    public ModIterableArrayList() {
    }

    public ModIterableArrayList(int initialCapacity) {
        super(initialCapacity);
    }

    public ModIterableArrayList(@NotNull Collection<? extends E> c) {
        super(c);
    }

    @NotNull
    @Override
    public Iterator<E> iterator() {
        return new ModIterator<>(this);
    }

    @Override
    public Spliterator<E> spliterator() {
        return new ModSpliterator<>(this);
    }

    @Override
    public void forEach(Consumer<? super E> action) {
        for(int i=0, size; i<(size = size()); i++) {
            action.accept(get(i));
            if(size() < size) i--;
        }
    }

    public static void main(String[] args) {
        List<Integer> list = new ModIterableArrayList<>();
        list.add(0);
        list.add(1);
        list.add(2);
        IntWrapper i = new IntWrapper(3);
        list.stream().forEach(j -> { Console.log(j);if(j<10) list.add(0, i.value++); });
        System.out.println(list);
    }
}
