package de.rccookie.util.persistent;

import java.util.Map;
import java.util.function.Function;

import de.rccookie.util.Arguments;

public class PersistentMapView<K,V> extends AbstractPersistentView<Map<K,V>> implements PersistentMap<K,V> {
    public PersistentMapView(PersistentData<?> backingData, Map<K, V> map) {
        super(backingData, Arguments.checkNull(map, "map"));
    }

    public <U> PersistentMapView(PersistentData<U> backingData, Function<? super U, ? extends Map<K, V>> map) {
        super(backingData, Arguments.checkNull(map, "map"));
    }
}
