package de.rccookie.util;

import java.util.Objects;

@SuppressWarnings("NewClassNamingConvention")
public class T2<A,B> {

    public final A a;
    public final B b;

    public T2(A a, B b) {
        this.a = a;
        this.b = b;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof T2)) return false;
        T2<?, ?> t2 = (T2<?, ?>) o;
        return Objects.equals(a, t2.a) && Objects.equals(b, t2.b);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ')';
    }
}
