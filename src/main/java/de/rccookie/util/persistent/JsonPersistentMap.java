package de.rccookie.util.persistent;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import de.rccookie.json.Json;
import de.rccookie.json.JsonDeserializer;
import de.rccookie.json.JsonSerializer;
import de.rccookie.json.linked.LinkedDeserializer;
import de.rccookie.json.linked.LinkedSerializer;
import de.rccookie.util.Arguments;

public class JsonPersistentMap<K,V> extends PersistentContainer<Map<K,V>> implements PersistentMap<K,V> {

    private static final JsonSerializer SERIALIZER = new LinkedSerializer();
    private static final JsonDeserializer DESERIALIZER = new LinkedDeserializer();

    public JsonPersistentMap(Path file, Class<K> keyType, Class<V> valueType) {
        this(file, (Type) keyType, valueType);
    }

    public JsonPersistentMap(Path file, Type keyType, Type valueType) {
        this(file, keyType, valueType, HashMap::new);
    }

    public JsonPersistentMap(Path file, Class<K> keyType, Class<V> valueType, Supplier<? extends Map<K,V>> mapCtor) {
        this(file, (Type) keyType, valueType, mapCtor);
    }

    public JsonPersistentMap(Path file, Type keyType, Type valueType, Supplier<? extends Map<K,V>> mapCtor) {
        super(
                file,
                self -> new PersistentMapView<>(self, $ -> mapCtor.get()),
                data -> Json.toString(((PersistentMapView<K,V>) data).data, true, SERIALIZER),
                (str, data) -> {
                    Json.parse(str).withDeserializer(DESERIALIZER).asCustomMap(() -> data, Arguments.checkNull(keyType, "keyType"), Arguments.checkNull(valueType, "valueType"));
                    return data;
                },
                data -> {
                    ((PersistentMapView<K,V>) data).data.clear();
                    return data;
                }
        );
    }

    @Override
    public boolean equals(Object obj) {
        return data.equals(obj);
    }

    @Override
    public int hashCode() {
        return data.hashCode();
    }
}
