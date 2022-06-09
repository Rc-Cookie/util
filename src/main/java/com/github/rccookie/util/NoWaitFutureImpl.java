package com.github.rccookie.util;

public class NoWaitFutureImpl<V> extends AbstractFutureImpl<V> {

    @Override
    public V waitFor() throws IllegalStateException, UnsupportedOperationException {
        throw new UnsupportedOperationException("waitFor() not supported");
    }
}
