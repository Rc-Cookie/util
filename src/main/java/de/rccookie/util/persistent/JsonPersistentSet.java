package de.rccookie.util.persistent;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import de.rccookie.json.Json;
import de.rccookie.json.JsonDeserializer;
import de.rccookie.json.JsonSerializer;
import de.rccookie.json.linked.LinkedDeserializer;
import de.rccookie.json.linked.LinkedSerializer;
import de.rccookie.util.Arguments;

public class JsonPersistentSet<T> extends PersistentContainer<Set<T>> implements PersistentSet<T> {

    private static final JsonSerializer SERIALIZER = new LinkedSerializer();
    private static final JsonDeserializer DESERIALIZER = new LinkedDeserializer();

    public JsonPersistentSet(Path file, Class<T> contentType) {
        this(file, (Type) contentType);
    }

    public JsonPersistentSet(Path file, Type contentType) {
        this(file, contentType, HashSet::new);
    }

    public JsonPersistentSet(Path file, Class<T> contentType, Supplier<? extends Set<T>> listCtor) {
        this(file, (Type) contentType, listCtor);
    }

    public JsonPersistentSet(Path file, Type contentType, Supplier<? extends Set<T>> listCtor) {
        super(
                file,
                self -> new PersistentSetView<>(self, $ -> listCtor.get()),
                data -> Json.toString(((PersistentSetView<T>) data).data, true, SERIALIZER),
                (str, data) -> {
                    Json.parse(str).withDeserializer(DESERIALIZER).asCollection(() -> data, Arguments.checkNull(contentType, "contentType"));
                    return data;
                },
                data -> {
                    ((PersistentSetView<T>) data).data.clear();
                    return data;
                }
        );
    }

    @Override
    public String toString() {
        return data.toString();
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
