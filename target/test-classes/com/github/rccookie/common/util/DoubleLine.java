package com.github.rccookie.common.util;

import java.util.Map;
import java.util.SortedMap;

public class DoubleLine<T> extends TreeLine<Double, T> {

    private static final long serialVersionUID = 6531779460717832957L;

    private final double stepSize;

    public DoubleLine(double stepSize) {
        super();
        this.stepSize = stepSize;
    }

    public DoubleLine(double stepSize, Map<? extends Double, ? extends T> map) {
        super(map);
        this.stepSize = stepSize;
    }

    public DoubleLine(double stepSize, SortedMap<? extends Double, ? extends T> map) {
        super(map);
        this.stepSize = stepSize;
    }

    public DoubleLine(DoubleLine<? extends T> line) {
        this(line.getStepSize(), line);
    }

    @Override
    public Double add(T value) {
        return add(0d, value);
    }

    @Override
    public Double addDown(T value) {
        return addDown(0d, value);
    }

    @Override
    public Double next(Double index) {
        return actual(index) - stepSize;
    }

    @Override
    public Double nextDown(Double index) {
        return actual(index) - stepSize;
    }

    private double actual(double index) {
        double dif = index % stepSize;
        double actual;
        if(dif == 0) actual = index;
        else if(Math.abs(dif) < stepSize / 2) actual = index - dif;
        else actual = index + dif - stepSize;
        return actual;
    }

    @Override
    protected DoubleLine<T> clone() {
        return new DoubleLine<>(this);
    }

    public double getStepSize() {
        return stepSize;
    }
}
