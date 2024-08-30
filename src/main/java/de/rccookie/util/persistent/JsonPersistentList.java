package de.rccookie.util.persistent;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import de.rccookie.json.Json;
import de.rccookie.json.JsonDeserializer;
import de.rccookie.json.JsonSerializer;
import de.rccookie.json.linked.LinkedDeserializer;
import de.rccookie.json.linked.LinkedSerializer;
import de.rccookie.util.Arguments;

public class JsonPersistentList<T> extends PersistentContainer<List<T>> implements PersistentList<T> {

    private static final JsonSerializer SERIALIZER = new LinkedSerializer();
    private static final JsonDeserializer DESERIALIZER = new LinkedDeserializer();

    public JsonPersistentList(Path file, Class<T> contentType) {
        this(file, (Type) contentType);
    }

    public JsonPersistentList(Path file, Type contentType) {
        this(file, contentType, ArrayList::new);
    }

    public JsonPersistentList(Path file, Class<T> contentType, Supplier<? extends List<T>> listCtor) {
        this(file, (Type) contentType, listCtor);
    }

    public JsonPersistentList(Path file, Type contentType, Supplier<? extends List<T>> listCtor) {
        super(
                file,
                self -> new PersistentListView<>(self, $ -> listCtor.get()),
                data -> Json.toString(((PersistentListView<T>) data).data, true, SERIALIZER),
                (str, data) -> {
                    Json.parse(str).withDeserializer(DESERIALIZER).asCollection(() -> data, Arguments.checkNull(contentType, "contentType"));
                    return data;
                },
                data -> {
                    ((PersistentListView<T>) data).data.clear();
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
