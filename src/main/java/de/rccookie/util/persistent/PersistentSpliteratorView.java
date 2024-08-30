package de.rccookie.util.persistent;

import java.util.Comparator;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public final class PersistentSpliteratorView<T> extends AbstractPersistentView<Spliterator<T>> implements Spliterator<T> {

    public <U> PersistentSpliteratorView(PersistentData<? extends U> backingData, Function<? super U, ? extends Spliterator<T>> iterator) {
        super(
                Arguments.checkNull(backingData, "backingData"),
                Arguments.checkNull(backingData.readLocked(Arguments.checkNull(iterator, "iterator")), "<returned iterator>")
        );
    }

    public PersistentSpliteratorView(PersistentData<?> data, Spliterator<T> spliterator) {
        super(data, Arguments.checkNull(spliterator, "spliterator"));
    }

    @Override
    public boolean tryAdvance(Consumer<? super T> action) {
        return readLocked(sp -> sp.tryAdvance(action));
    }

    @Override
    public void forEachRemaining(Consumer<? super T> action) {
        doReadLocked(sp -> sp.forEachRemaining(action));
    }

    @Override
    public Spliterator<T> trySplit() {
        return readLocked(sp -> {
            Spliterator<T> split = sp.trySplit();
            return split != null ? new PersistentSpliteratorView<>(backingData, split) : null;
        });
    }

    @Override
    public long estimateSize() {
        return readLocked(Spliterator::estimateSize);
    }

    @Override
    public long getExactSizeIfKnown() {
        return readLocked(Spliterator::getExactSizeIfKnown);
    }

    @Override
    public int characteristics() {
        return data.characteristics();
    }

    @Override
    public Comparator<? super T> getComparator() {
        return data.getComparator();
    }
}
