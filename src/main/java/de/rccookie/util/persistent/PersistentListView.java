package de.rccookie.util.persistent;

import java.util.List;
import java.util.function.Function;

public class PersistentListView<T> extends AbstractPersistentView<List<T>> implements PersistentList<T> {

    public PersistentListView(PersistentData<?> backingData, List<T> data) {
        super(backingData, data);
    }

    public <U> PersistentListView(PersistentData<U> backingData, Function<? super U, ? extends List<T>> dataGetter) {
        super(backingData, dataGetter);
    }
}
