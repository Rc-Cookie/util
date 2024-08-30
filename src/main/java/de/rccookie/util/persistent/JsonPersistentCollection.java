package de.rccookie.util.persistent;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.Collection;
import java.util.function.Supplier;

import de.rccookie.json.Json;
import de.rccookie.json.JsonDeserializer;
import de.rccookie.json.JsonSerializer;
import de.rccookie.json.linked.LinkedDeserializer;
import de.rccookie.json.linked.LinkedSerializer;
import de.rccookie.util.Arguments;

public class JsonPersistentCollection<T> extends PersistentContainer<Collection<T>> implements PersistentCollection<T> {

    private static final JsonSerializer SERIALIZER = new LinkedSerializer();
    private static final JsonDeserializer DESERIALIZER = new LinkedDeserializer();

    public JsonPersistentCollection(Path file, Class<T> contentType, Supplier<? extends Collection<T>> listCtor) {
        this(file, (Type) contentType, listCtor);
    }

    public JsonPersistentCollection(Path file, Type contentType, Supplier<? extends Collection<T>> listCtor) {
        super(
                file,
                self -> new PersistentCollectionView<>(self, $ -> listCtor.get()),
                data -> Json.toString(((PersistentCollectionView<T>) data).data, true, SERIALIZER),
                (str, data) -> {
                    Json.parse(str).withDeserializer(DESERIALIZER).asCollection(() -> data, Arguments.checkNull(contentType, "contentType"));
                    return data;
                },
                data -> {
                    ((PersistentCollectionView<T>) data).data.clear();
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
