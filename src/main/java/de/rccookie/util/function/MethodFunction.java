package de.rccookie.util.function;

import java.util.function.Function;

@FunctionalInterface
public interface MethodFunction<R> extends Function<Object[],R> {

    @Override
    default R apply(Object[] params) {
        return invoke(params);
    }

    default R invoke(Object... params) {
        return invokeOn(null, params);
    }

    R invokeOn(Object target, Object... params);
}
