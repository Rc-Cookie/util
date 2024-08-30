package de.rccookie.util.persistent;

import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.function.Supplier;

import de.rccookie.json.Json;
import de.rccookie.json.JsonDeserializer;
import de.rccookie.json.JsonSerializer;
import de.rccookie.json.linked.LinkedDeserializer;
import de.rccookie.json.linked.LinkedSerializer;

public class JsonPersistentValue<T> extends PersistentContainer<T> implements PersistentValue<T> {

    private static final JsonSerializer SERIALIZER = new LinkedSerializer();
    private static final JsonDeserializer DESERIALIZER = new LinkedDeserializer();

    public JsonPersistentValue(Path file, Type type, Supplier<? extends T> defaultValue) {
        super(
                file,
                self -> defaultValue.get(),
                d -> Json.toString(d, true, SERIALIZER),
                (s,d) -> Json.parse(s).withDeserializer(DESERIALIZER).as(type),
                d -> defaultValue.get()
        );
    }

    @Override
    public T set(T value) {
        return writeLocked(() -> data = value);
    }
}
