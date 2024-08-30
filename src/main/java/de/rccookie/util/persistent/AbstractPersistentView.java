package de.rccookie.util.persistent;

import java.util.concurrent.locks.ReadWriteLock;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public abstract class AbstractPersistentView<T> extends AbstractPersistentData<T> {

    protected final PersistentData<?> backingData;
    protected T data;

    public AbstractPersistentView(PersistentData<?> backingData, T data) {
        Arguments.checkNull(backingData, "backingData");
        while(backingData instanceof AbstractPersistentView)
            backingData = ((AbstractPersistentView<?>) backingData).backingData;
        this.backingData = backingData;
        this.data = Arguments.checkNull(data, "data");
    }

    public <U> AbstractPersistentView(PersistentData<U> backingData, Function<? super U, ? extends T> dataGetter) {
        this(
                Arguments.checkNull(backingData, "backingData"),
                Arguments.checkNull(backingData.readLocked(Arguments.checkNull(dataGetter, "dataGetter")), "<returned data>")
        );
    }

    @Override
    public boolean equals(Object obj) {
        return readLocked(t -> t.equals(obj));
    }

    @Override
    public int hashCode() {
        return readLocked(T::hashCode);
    }

    @Override
    protected T data() {
        return data;
    }

    @Override
    public void markDirty() {
        backingData.markDirty();
    }

    @Override
    public void reload() {
        backingData.reload();
    }

    @Override
    public ReadWriteLock lock() {
        return backingData.lock();
    }
}
