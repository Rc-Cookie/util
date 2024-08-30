package de.rccookie.util.persistent;

import java.nio.file.Path;

public interface PersistentObject<T extends PersistentObject<T>> extends PersistentData<T> {
    static <T extends PersistentObject<T>> T loadJson(Class<T> type, Path file) {
        return PersistentObjectUtils.loadJson(type, file);
    }
}
