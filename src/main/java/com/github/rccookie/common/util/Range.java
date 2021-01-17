package com.github.rccookie.common.util;

import java.util.Iterator;

public interface Range<N extends Number> extends Iterable<N> {

    public static Range<Long> of(double start, double end) {
        return new Range<Long>() {
			public Iterator<Long> iterator() {
				return new Iterator<Long>() {
                    private final long myEnd = (long)end;
                    private long now = (long)start;
                    public boolean hasNext() { return now != myEnd; }
                    public Long next() { return now++; }
                };
			}
        };
    }

    public static Range<Long> of(double length) {
        return of((long)0, length);
    }


    /**
     * Returns a Range from {@code start} (inclusive) to {@code end} (exclusive).
     * <p>For example:
     * <pre>
     * for(int i : Range.of(50, 100)) {
     *     //...
     * }
     * </pre>
     * 
     * @param start The first number in the range
     * @param end The first number not to be in the range
     * @return A range with the specified dimension
     */
    public static Range<Integer> of(int start, int end) {
        return new Range<Integer>() {
			public Iterator<Integer> iterator() {
				return new Iterator<Integer>() {
                    private final int myEnd = end;
                    private int now = start;
                    public boolean hasNext() { return now != myEnd; }
                    public Integer next() { return now++; }
                };
			}
        };
    }

    /**
     * Returns a Range from {@code 0} (inclusive) to {@code end} (exclusive).
     * <p>For example:
     * <pre>
     * for(int i : Range.of(100)) {
     *     //...
     * }
     * </pre>
     * 
     * @param length The number of numbers to include
     * @return A range with the specified dimension
     */
    public static Range<Integer> of(int length) {
        return of(0, length);
    }


    public static Range<Short> of(short start, short end) {
        return new Range<Short>() {
			public Iterator<Short> iterator() {
				return new Iterator<Short>() {
                    private final short myEnd = end;
                    private short now = start;
                    public boolean hasNext() { return now != myEnd; }
                    public Short next() { return now++; }
                };
			}
        };
    }

    public static Range<Short> of(short length) {
        return of((short)0, length);
    }


    public static Range<Byte> of(byte start, byte end) {
        return new Range<Byte>() {
			public Iterator<Byte> iterator() {
				return new Iterator<Byte>() {
                    private final byte myEnd = end;
                    private byte now = start;
                    public boolean hasNext() { return now != myEnd; }
                    public Byte next() { return now++; }
                };
			}
        };
    }

    public static Range<Byte> of(byte length) {
        return of((byte)0, length);
    }
}
