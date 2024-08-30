package de.rccookie.util.persistent;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Proxy;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import de.rccookie.json.Json;
import de.rccookie.json.JsonElement;
import de.rccookie.json.JsonSerializer;
import de.rccookie.json.linked.LinkedDeserializer;
import de.rccookie.json.linked.LinkedSerializer;
import de.rccookie.util.Utils;
import de.rccookie.util.Wrapper;
import de.rccookie.util.function.MethodFunction;

final class PersistentObjectUtils {

    private PersistentObjectUtils() { }


    private static final Method MARK_DIRTY, RELOAD, LOCK, READ_LOCKED, DO_READ_LOCKED, WRITE_LOCKED, TEST_WRITE_LOCKED, DO_WRITE_LOCKED, EQUALS, HASH_CODE, TO_STRING;
    static {
        try {
            MARK_DIRTY = PersistentData.class.getMethod("markDirty");
            RELOAD = PersistentData.class.getMethod("reload");
            LOCK = PersistentData.class.getMethod("lock");
            READ_LOCKED = PersistentData.class.getMethod("readLocked", Function.class);
            DO_READ_LOCKED = PersistentData.class.getMethod("doReadLocked", Consumer.class);
            WRITE_LOCKED = PersistentData.class.getMethod("writeLocked", Function.class);
            TEST_WRITE_LOCKED = PersistentData.class.getMethod("testWriteLocked", Predicate.class);
            DO_WRITE_LOCKED = PersistentData.class.getMethod("doWriteLocked", Consumer.class);
            EQUALS = Object.class.getMethod("equals", Object.class);
            HASH_CODE = Object.class.getMethod("hashCode");
            TO_STRING = Object.class.getMethod("toString");
        } catch(NoSuchMethodException e) {
            throw Utils.rethrow(e);
        }
    }


    private static MethodFunction<?> getDefaultMethod(Class<?> interfaceType, Method method) {
        try {
            MethodHandle handle = MethodHandles.lookup().findSpecial(
                    interfaceType,
                    method.getName(),
                    MethodType.methodType(
                            method.getReturnType(),
                            method.getParameterTypes()
                    ),
                    interfaceType
            );
            return (target, params) -> {
                try {
                    return handle.bindTo(target).invokeWithArguments(params);
                } catch(Throwable e) {
                    throw Utils.rethrow(e);
                }
            };
        } catch(Exception e) {
            throw Utils.rethrow(e);
        }
    }


    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T extends PersistentObject> T loadJson(Class<T> type, Path file) {

        if(!type.isInterface())
            throw new IllegalArgumentException("Type to load must be interface");


        Map<String, JsonProxy> fieldProxies = new HashMap<>();
        Map<String, Method> methods = new HashMap<>();

        for(Method m : type.getDeclaredMethods()) {
            if(m.isDefault() && !m.isAnnotationPresent(Persistent.class))
                continue;
            if(m.getParameterCount() != 0)
                throw new IllegalArgumentException("Non-default methods may not have parameters");
            BiFunction<PersistentData<?>, PersistentData<?>, ?> defaultValue = null;
            if(m.isDefault()) {
                MethodFunction<?> defaultMethod = getDefaultMethod(type, m);
                defaultValue = (proxy, owner) -> defaultMethod.invokeOn(proxy);
            }
            fieldProxies.put(m.getName(), JsonProxy.forType(m.getReturnType(), m.getGenericReturnType(), defaultValue));
            methods.put(m.getName(), m);
        }


        JsonSerializer serializer = new LinkedSerializer();
        LinkedDeserializer deserializer = new LinkedDeserializer();


        Wrapper<Map<String, Object>> values = new Wrapper<>(new HashMap<>());
        Wrapper<PersistentData<Map<String, Object>>> data = new Wrapper<>();

        T proxy = (T) Proxy.newProxyInstance(type.getClassLoader(), new Class[] { type }, (o, m, a) -> {

            if(m.isDefault() && !m.isAnnotationPresent(Persistent.class))
                return getDefaultMethod(type, m).invokeOn(o,a);

            if(m.equals(EQUALS)) {
                if(o == a[0])
                    return true;
                if(o.getClass() != a[0].getClass())
                    return false;
                Map<String, Object> otherValues = new HashMap<>();
                methods.forEach((n,method) -> otherValues.put(n, Utils.invoke(a[0], method.getName())));
                return values.value.equals(otherValues);
            }
            if(m.equals(HASH_CODE))
                return values.value.hashCode();
            if(m.equals(TO_STRING))
                return o.getClass().getName() + '@' + Integer.toHexString(o.hashCode());

            if(m.equals(MARK_DIRTY)) {
                data.value.markDirty();
                return null;
            }
            if(m.equals(RELOAD)) {
                data.value.reload();
                return null;
            }
            if(m.equals(LOCK))
                return data.value.lock();
            if(m.equals(READ_LOCKED))
                return data.value.readLocked(() -> ((Function<Object,?>) a[0]).apply(o));
            if(m.equals(DO_READ_LOCKED)) {
                data.value.doReadLocked(() -> ((Consumer<Object>) a[0]).accept(o));
                return null;
            }
            if(m.equals(WRITE_LOCKED))
                return data.value.writeLocked(() -> ((Function<Object,?>) a[0]).apply(o));
            if(m.equals(TEST_WRITE_LOCKED))
                return data.value.testWriteLocked(() -> ((Predicate<Object>) a[0]).test(o));
            if(m.equals(DO_WRITE_LOCKED)) {
                data.value.doWriteLocked(() -> ((Consumer<Object>) a[0]).accept(o));
                return null;
            }

            return values.value.get(m.getName());
        });

        data.value = new PersistentContainer<>(
                file,
                self -> values.value,
                d -> Json.usingSerializer(serializer, () -> {
                    Map<String, Object> obj = new HashMap<>();
                    fieldProxies.forEach((n, p) -> obj.put(n, p.serialize(values.value.get(n))));
                    return Json.toString(obj);
                }),
                (s,d,self) -> {
                    JsonElement json = deserializer.wrap(Json.parse(s));
                    Map<String, Object> newValues = new HashMap<>();
                    fieldProxies.forEach((n,p) -> newValues.put(n, p.deserialize(json.get(n), self)));
                    return values.value = newValues;
                },
                (d,self) -> {
                    fieldProxies.forEach((n,p) -> d.put(n, p.defaultValue(proxy, self)));
                    return d;
                }
        ) { };

        return proxy;
    }


    static Type[] typeParameters(Type type) {
        if(!(type instanceof ParameterizedType))
            return new Type[0];
        return ((ParameterizedType) type).getActualTypeArguments();
    }

    static Type withoutWildcards(Type type) {
        while(type instanceof WildcardType) {
            WildcardType w = (WildcardType) type;
            Type[] bounds = w.getLowerBounds();
            if(bounds == null)
                bounds = w.getUpperBounds();
            if(bounds == null)
                return Object.class;
            type = bounds[0];
        }
        return type;
    }
}

