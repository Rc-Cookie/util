package de.rccookie.util;

import java.util.Objects;

@SuppressWarnings("NewClassNamingConvention")
public class T4<A,B,C,D> {

    public final A a;
    public final B b;
    public final C c;
    public final D d;

    public T4(A a, B b, C c, D d) {
        this.a = a;
        this.b = b;
        this.c = c;
        this.d = d;
    }

    @Override
    public boolean equals(Object o) {
        if(this == o) return true;
        if(!(o instanceof T4)) return false;
        T4<?, ?, ?, ?> t4 = (T4<?, ?, ?, ?>) o;
        return Objects.equals(a, t4.a) && Objects.equals(b, t4.b) && Objects.equals(c, t4.c) && Objects.equals(d, t4.d);
    }

    @Override
    public int hashCode() {
        return Objects.hash(a, b, c, d);
    }

    @Override
    public String toString() {
        return "(" + a + ", " + b + ", " + c + ", " + d + ')';
    }
}
