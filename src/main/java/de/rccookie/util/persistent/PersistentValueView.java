package de.rccookie.util.persistent;

import java.util.function.Function;

import de.rccookie.util.Arguments;

public class PersistentValueView<T> extends AbstractPersistentView<T> implements PersistentValue<T> {

    public PersistentValueView(PersistentData<?> backingData, T defaultValue) {
        super(backingData, defaultValue);
    }

    public <U> PersistentValueView(PersistentData<U> backingData, Function<? super U, ? extends T> defaultValue) {
        super(backingData, Arguments.checkNull(defaultValue, "defaultValue"));
    }

    @Override
    public T set(T value) {
        return writeLocked(() -> this.data = value);
    }
}
