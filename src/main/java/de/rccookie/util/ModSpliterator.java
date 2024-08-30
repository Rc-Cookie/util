package de.rccookie.util;

import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class ModSpliterator<T> implements Spliterator<T> {

    private final List<T> list;
    private final int limit;
    private int i = 0;

    public ModSpliterator(List<T> list) {
        this(list, 0, Integer.MAX_VALUE);
    }

    private ModSpliterator(List<T> list, int i, int limit) {
        this.list = Arguments.checkNull(list);
        this.i = i;
        this.limit = limit;
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        if(i < limit && i < list.size()) {
            action.accept(list.get(i++));
            return true;
        }
        return false;
    }

    @Override
    public Spliterator<T> trySplit() {
        int l = Math.min(limit, list.size());
        if(i >= l - 1) return null;
        int oLimit = (i + l) / 2;
        Spliterator<T> other = new ModSpliterator<>(list, i, oLimit);
        i = oLimit;
        return other;
    }

    @Override
    public long estimateSize() {
        return Math.min(list.size(), limit) - i;
    }

    @Override
    public int characteristics() {
        return Spliterator.ORDERED;
    }
}
