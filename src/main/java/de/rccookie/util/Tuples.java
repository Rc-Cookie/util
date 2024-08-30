package de.rccookie.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

public final class Tuples {

    private Tuples() {
        throw new UnsupportedOperationException();
    }

    @Contract(value = "_,_ -> new", pure = true)
    @NotNull
    public static <A,B> T2<A,B> t(A a, B b) {
        return new T2<>(a,b);
    }

    @Contract(value = "_,_,_ -> new", pure = true)
    @NotNull
    public static <A,B,C> T3<A,B,C> t(A a, B b, C c) {
        return new T3<>(a,b,c);
    }

    @Contract(value = "_,_,_,_ -> new", pure = true)
    @NotNull
    public static <A,B,C,D> T4<A,B,C,D> t(A a, B b, C c, D d) {
        return new T4<>(a,b,c,d);
    }
}
