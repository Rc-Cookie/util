package com.github.rccookie.util;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
    public static double checkRange(double given, @Nullable Double min, @Nullable Double max) {
        if(min == null) min = Double.NEGATIVE_INFINITY;
        if(max == null) max = Double.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given > max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max, true, false);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, inclusive. {@code null} means no maximum
     */
    public static double checkInclusive(double given, @Nullable Double min, @Nullable Double max) {
        if(min == null) min = Double.NEGATIVE_INFINITY;
        if(max == null) max = Double.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given > max)) || (inverted && given > max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max, true, true);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, exclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static double checkExclusive(double given, @Nullable Double min, @Nullable Double max) {
        if(min == null) min = Double.NEGATIVE_INFINITY;
        if(max == null) max = Double.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given <= min || given >= max)) || (inverted && given >= max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max, false, false);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static float checkRange(float given, @Nullable Float min, @Nullable Float max) {
        if(min == null) min = Float.NEGATIVE_INFINITY;
        if(max == null) max = Float.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given > max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max, true, false);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, inclusive. {@code null} means no maximum
     */
    public static float checkInclusive(float given, @Nullable Float min, @Nullable Float max) {
        if(min == null) min = Float.NEGATIVE_INFINITY;
        if(max == null) max = Float.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given > max)) || (inverted && given > max && given < min))
            throw new ArgumentOutOfRangeException(given, min, max, true, true);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, exclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static float checkExclusive(float given, @Nullable Float min, @Nullable Float max) {
        if(min == null) min = Float.NEGATIVE_INFINITY;
        if(max == null) max = Float.POSITIVE_INFINITY;
        final boolean inverted = min > max;
        if((!inverted && (given <= min || given >= max)) || (inverted && given >= max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max, false, false);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static long checkRange(long given, @Nullable Long min, @Nullable Long max) {
        if(min == null) min = Long.MIN_VALUE;
        if(max == null) max = Long.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given > max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max-1l);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static int checkRange(int given, @Nullable Integer min, @Nullable Integer max) {
        if(min == null) min = Integer.MIN_VALUE;
        if(max == null) max = Integer.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given > max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, max-1);
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static short checkRange(short given, @Nullable Short min, @Nullable Short max) {
        if(min == null) min = Short.MIN_VALUE;
        if(max == null) max = Short.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given > max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, (short)(max-1));
        return given;
    }

    /**
     * Checks weather the given number is in the specified range and throws
     * an {@link ArgumentOutOfRangeException} if it is not.
     *
     * @param given The number to check
     * @param min The minimum value, inclusive. {@code null} means no minimum
     * @param max The maximum value, exclusive. {@code null} means no maximum
     */
    public static byte checkRange(byte given, @Nullable Byte min, @Nullable Byte max) {
        if(min == null) min = Byte.MIN_VALUE;
        if(max == null) max = Byte.MAX_VALUE;
        final boolean inverted = min > max;
        if((!inverted && (given < min || given >= max)) || (inverted && given > max && given <= min))
            throw new ArgumentOutOfRangeException(given, min, (byte)(max-1));
        return given;
    }


    /**
     * Throws a {@link NullArgumentException} if the given value is {@code null},
     * otherwise the value itself will be returned.
     *
     * @param object The value to check for non-null
     * @param <T> The type of object
     * @return The passed value if it is not null
     */
    @Contract("null -> fail; !null -> param1")
    @NotNull
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
    @Contract("null, _ -> fail; !null, _ -> param1")
    @NotNull
    public static <T> T checkNull(T object, String parameterName) {
        if(object == null) throw new NullArgumentException(parameterName);
        return object;
    }
}
