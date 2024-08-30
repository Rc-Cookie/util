package de.rccookie.util.persistent;

import java.util.Set;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public class PersistentSetView<T> extends AbstractPersistentView<Set<T>> implements PersistentSet<T> {
    public PersistentSetView(PersistentData<?> backingData, Set<T> set) {
        super(backingData, Arguments.checkNull(set, "set"));
    }

    public <U> PersistentSetView(PersistentData<U> backingData, Function<? super U, ? extends Set<T>> set) {
        super(backingData, Arguments.checkNull(set, "set"));
    }
}
