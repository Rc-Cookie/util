package com.github.rccookie.util;

import java.util.function.Consumer;

@FunctionalInterface
public interface Computation<T> {

    T compute() throws Exception;

    default void tryCompute(Consumer<? super T> onSuccess, Consumer<? super Exception> onFailure) {
        try {
            onSuccess.accept(compute());
        } catch(Exception e) {
            onFailure.accept(e);
        }
    }
}
