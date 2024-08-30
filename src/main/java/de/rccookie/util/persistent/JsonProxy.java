package de.rccookie.util.persistent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.function.BiFunction;

import de.rccookie.json.Json;
import de.rccookie.json.JsonElement;

interface JsonProxy<T> {

    static JsonProxy<?> forType(Class<?> rawType, Type genericType, BiFunction<? super PersistentData<?>, ? super PersistentData<?>, ?> defaultValue) {
        if(rawType == PersistentList.class || rawType == PersistentCollection.class)
            return JsonProxy.forList(PersistentObjectUtils.typeParameters(genericType)[0], (BiFunction) defaultValue);
        if(rawType == PersistentSet.class)
            return JsonProxy.forSet(PersistentObjectUtils.typeParameters(genericType)[0], (BiFunction) defaultValue);
        if(rawType == PersistentMap.class) {
            Type[] typeParameters = PersistentObjectUtils.typeParameters(genericType);
            return JsonProxy.forMap(typeParameters[0], typeParameters[1], (BiFunction) defaultValue);
        }
        if(defaultValue == null)
            throw new IllegalArgumentException("Type " + genericType + " cannot be persistent without default value; declare method default and annotate with @Persistent");
        return forPrimitive(genericType, defaultValue);
    }

    static <T> JsonProxy<PersistentList<T>> forList(Type contentType, BiFunction<? super PersistentData<?>, ? super PersistentData<?>, ? extends PersistentList<T>> defaultValue) {
        Type concreteContentType = PersistentObjectUtils.withoutWildcards(contentType);
        return new JsonProxy<>() {
            @Override
            public Object serialize(PersistentList<T> value) {
                return value.readLocked(Json::serialize);
            }

            @Override
            public PersistentList<T> deserialize(JsonElement json, PersistentData<?> owner) {
                return new PersistentListView<>(owner, json.<T, ArrayList<T>>asCollection(ArrayList::new, concreteContentType));
            }

            @Override
            public PersistentList<T> defaultValue(PersistentData<?> proxy, PersistentData<?> owner) {
                if(defaultValue == null)
                    return new PersistentListView<>(proxy, new ArrayList<>());
                return defaultValue.apply(proxy, owner);
            }
        };
    }

    static <T> JsonProxy<PersistentSet<T>> forSet(Type contentType, BiFunction<? super PersistentData<?>, ? super PersistentData<?>, ? extends PersistentSet<T>> defaultValue) {
        Type concreteContentType = PersistentObjectUtils.withoutWildcards(contentType);
        return new JsonProxy<>() {
            @Override
            public Object serialize(PersistentSet<T> value) {
                return value.readLocked(Json::serialize);
            }

            @Override
            public PersistentSet<T> deserialize(JsonElement json, PersistentData<?> owner) {
                return new PersistentSetView<>(owner, json.<T, HashSet<T>>asCollection(HashSet::new, concreteContentType));
            }

            @Override
            public PersistentSet<T> defaultValue(PersistentData<?> proxy, PersistentData<?> owner) {
                if(defaultValue == null)
                    return new PersistentSetView<>(proxy, new HashSet<>());
                return defaultValue.apply(proxy, owner);
            }
        };
    }

    static <K, V> JsonProxy<PersistentMap<K, V>> forMap(Type keyType, Type valueType, BiFunction<? super PersistentData<?>, ? super PersistentData<?>, ? extends PersistentMap<K, V>> defaultValue) {
        Type concreteKeyType = PersistentObjectUtils.withoutWildcards(keyType);
        Type concreteValueType = PersistentObjectUtils.withoutWildcards(valueType);
        return new JsonProxy<>() {
            @Override
            public Object serialize(PersistentMap<K, V> value) {
                return value.readLocked(Json::serialize);
            }

            @Override
            public PersistentMap<K, V> deserialize(JsonElement json, PersistentData<?> owner) {
                return new PersistentMapView<>(owner, json.<K, V, HashMap<K, V>>asCustomMap(HashMap::new, concreteKeyType, concreteValueType));
            }

            @Override
            public PersistentMap<K, V> defaultValue(PersistentData<?> proxy, PersistentData<?> owner) {
                if(defaultValue == null)
                    return new PersistentMapView<>(proxy, new HashMap<>());
                return defaultValue.apply(proxy, owner);
            }
        };
    }

    static <T> JsonProxy<PersistentValue<T>> forValue(Type contentType, BiFunction<? super PersistentData<?>, ? super PersistentData<?>, ? extends PersistentValue<T>> defaultValue) {
        Type concreteContentType = PersistentObjectUtils.withoutWildcards(contentType);
        return new JsonProxy<>() {
            @Override
            public Object serialize(PersistentValue<T> value) {
                return value.readLocked(Json::serialize);
            }

            @Override
            public PersistentValue<T> deserialize(JsonElement json, PersistentData<?> owner) {
                return new PersistentValueView<>(owner, json.as(concreteContentType));
            }

            @Override
            public PersistentValue<T> defaultValue(PersistentData<?> proxy, PersistentData<?> owner) {
                return defaultValue.apply(proxy, owner);
            }
        };
    }

    static <T> JsonProxy<T> forPrimitive(Type type, BiFunction<? super PersistentData<?>, ? super PersistentData<?>, ? extends T> defaultValue) {
        Type concreteType = PersistentObjectUtils.withoutWildcards(type);
        return new JsonProxy<>() {
            @Override
            public Object serialize(T value) {
                return Json.serialize(value);
            }

            @Override
            public T deserialize(JsonElement json, PersistentData<?> owner) {
                return json.as(concreteType);
            }

            @Override
            public T defaultValue(PersistentData<?> proxy, PersistentData<?> owner) {
                return defaultValue.apply(proxy, owner);
            }
        };
    }

    Object serialize(T value);

    T deserialize(JsonElement json, PersistentData<?> owner);

    T defaultValue(PersistentData<?> proxy, PersistentData<?> owner);
}
