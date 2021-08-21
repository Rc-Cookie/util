package com.github.rccookie.util;

public final class Arguments {

    private Arguments() {
        throw new UnsupportedOperationException();
    }



    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkRange(double given, Double min, Double max) {
        if(min == null) min = Double.NEGATIVE_INFINITY;
        if(max == null) max = Double.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given >= max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, inclusive. {@code null} means no maximum
     */
    public static void checkInclusive(double given, Double min, Double max) {
        if(min == null) min = Double.NEGATIVE_INFINITY;
        if(max == null) max = Double.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given > max)) || (inverted && given >= max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, exclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkExclusive(double given, Double min, Double max) {
        if(min == null) min = Double.NEGATIVE_INFINITY;
        if(max == null) max = Double.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given <= min || given >= max)) || (inverted && given > max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkRange(float given, Float min, Float max) {
        if(min == null) min = Float.NEGATIVE_INFINITY;
        if(max == null) max = Float.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given >= max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, inclusive. {@code null} means no maximum
     */
    public static void checkInclusive(float given, Float min, Float max) {
        if(min == null) min = Float.NEGATIVE_INFINITY;
        if(max == null) max = Float.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given > max)) || (inverted && given >= max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, exclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkExclusive(float given, Float min, Float max) {
        if(min == null) min = Float.NEGATIVE_INFINITY;
        if(max == null) max = Float.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given <= min || given >= max)) || (inverted && given > max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkRange(long given, Long min, Long max) {
        if(min == null) min = Long.MIN_VALUE;
        if(max == null) max = Long.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given >= max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkRange(int given, Integer min, Integer max) {
        if(min == null) min = Integer.MIN_VALUE;
        if(max == null) max = Integer.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given >= max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkRange(short given, Short min, Short max) {
        if(min == null) min = Short.MIN_VALUE;
        if(max == null) max = Short.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given >= max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static void checkRange(byte given, Byte min, Byte max) {
        if(min == null) min = Byte.MIN_VALUE;
        if(max == null) max = Byte.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given >= max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max);
    }


    /**
     * Throws a {@link NullArgumentException} if the given value is {@code null},
     * otherwise the value itself will be returned.
     *
     * @param object The value to check for non-null
     * @param <T> The type of object
     * @return The passed value if it is not null
     */
    public static <T> T checkNull(T object) {
        return checkNull(object, null);
    }

    /**
     * Throws a {@link NullArgumentException} if the given value is {@code null},
     * otherwise the value itself will be returned.
     *
     * @param object The value to check for non-null
     * @param parameterName The name of the variable that is checked for null
     * @param <T> The type of object
     * @return The passed value if it is not null
     */
    public static <T> T checkNull(T object, String parameterName) {
        if(object == null) throw new NullArgumentException(parameterName);
        return object;
    }
}
