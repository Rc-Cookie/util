package de.rccookie.util;

import java.util.Objects;

@SuppressWarnings("NewClassNamingConvention")
public class T3<A,B,C> {

    public final A a;
    public final B b;
    public final C c;

    public T3(A a, B b, C c) {
        this.a = a;
        this.b = b;
        this.c = c;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof T3)) return false;
        T3<?, ?, ?> t3 = (T3<?, ?, ?>) o;
        return Objects.equals(a, t3.a) && Objects.equals(b, t3.b) && Objects.equals(c, t3.c);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ')';
    }
}
