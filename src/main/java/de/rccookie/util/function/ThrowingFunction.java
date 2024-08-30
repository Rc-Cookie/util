package de.rccookie.util.function;

public interface ThrowingFunction<I,R,T extends Throwable> {

    R apply(I x) throws T;
}
