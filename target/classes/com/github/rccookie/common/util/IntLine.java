package com.github.rccookie.common.util;

import java.util.Map;
import java.util.SortedMap;

public class IntLine<T> extends TreeLine<Integer, T> {

    private static final long serialVersionUID = 8894072576450499022L;

    public IntLine() { super(); }

    public IntLine(Map<? extends Integer, ? extends T> map) { super(map); }

    public IntLine(SortedMap<? extends Integer, ? extends T> map) { super(map); }

    @Override
    public Integer add(T value) {
        return add(0, value);
    }

    @Override
    public Integer addDown(T value) {
        return addDown(0, value);
    }

    @Override
    protected IntLine<T> clone() {
        return new IntLine<>(this);
    }

    @Override
    public Integer next(Integer index) {
        return index + 1;
    }

    @Override
    public Integer nextDown(Integer index) {
        return index - 1;
    }
}
