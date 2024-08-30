package de.rccookie.util.persistent;

import java.util.function.Function;

public interface PersistentValue<T> extends PersistentData<T> {


    default T get() {
        return readLocked(Function.identity());
    }

    T set(T value);
}
