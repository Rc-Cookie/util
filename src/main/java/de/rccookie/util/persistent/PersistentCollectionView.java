package de.rccookie.util.persistent;

import java.util.Collection;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public class PersistentCollectionView<T> extends AbstractPersistentView<Collection<T>> implements PersistentCollection<T> {

    public PersistentCollectionView(PersistentData<?> backingData, Collection<T> collection) {
        super(backingData, Arguments.checkNull(collection, "collection"));
    }

    public <U> PersistentCollectionView(PersistentData<U> backingData, Function<? super U, ? extends Collection<T>> collection) {
        super(backingData, Arguments.checkNull(collection, "collection"));
    }
}
