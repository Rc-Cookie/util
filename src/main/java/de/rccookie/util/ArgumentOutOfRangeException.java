package de.rccookie.util;

/**
 * Exception indicating that a given argument, most likely a number, was outside
 * the allowed range.
 */
public class ArgumentOutOfRangeException extends IllegalArgumentException {

    /**
     * Constructs a new ArgumentOutOfRangeException with no detail message.
     */
    public ArgumentOutOfRangeException() {
        super();
    }

    /**
     * Constructs a new ArgumentOutOfRangeException with the given message.
     *
     * @param message The message explaining the reason of the exception
     */
    public ArgumentOutOfRangeException(String message) {
        super(message);
    }



    /**
     * Constructs a new ArgumentOutOfRangeException with a generated message.
     *
     * @param given The number that was passed and is out of range
     * @param min The lowest allowed number. {@code null} will be displayed as -∞
     * @param max The highest allowed number. {@code null} will be displayed as ∞
     */
    public ArgumentOutOfRangeException(double given, Double min, Double max, boolean startIncl, boolean endIncl) {
        this(getMessageFor(given, min, max, startIncl, endIncl));
    }

    /**
     * Constructs a new ArgumentOutOfRangeException with a generated message.
     *
     * @param given The number that was passed and is out of range
     * @param min The lowest allowed number. {@code null} will be displayed as -∞
     * @param max The highest allowed number. {@code null} will be displayed as ∞
     */
    public ArgumentOutOfRangeException(float given, Float min, Float max, boolean startIncl, boolean endIncl) {
        this(getMessageFor(given, min, max, startIncl, endIncl));
    }

    /**
     * Constructs a new ArgumentOutOfRangeException with a generated message.
     *
     * @param given The number that was passed and is out of range
     * @param min The lowest allowed number. {@code null} will be displayed as -∞
     * @param max The highest allowed number. {@code null} will be displayed as ∞
     */
    public ArgumentOutOfRangeException(long given, Long min, Long max) {
        this(getMessageFor(given, min, max));
    }

    /**
     * Constructs a new ArgumentOutOfRangeException with a generated message.
     *
     * @param given The number that was passed and is out of range
     * @param min The lowest allowed number. {@code null} will be displayed as -∞
     * @param max The highest allowed number. {@code null} will be displayed as ∞
     */
    public ArgumentOutOfRangeException(int given, Integer min, Integer max) {
        this(getMessageFor(given, min, max));
    }

    /**
     * Constructs a new ArgumentOutOfRangeException with a generated message.
     *
     * @param given The number that was passed and is out of range
     * @param min The lowest allowed number. {@code null} will be displayed as -∞
     * @param max The highest allowed number. {@code null} will be displayed as ∞
     */
    public ArgumentOutOfRangeException(short given, Short min, Short max) {
        this(getMessageFor(given, min, max));
    }

    /**
     * Constructs a new ArgumentOutOfRangeException with a generated message.
     *
     * @param given The number that was passed and is out of range
     * @param min The lowest allowed number. {@code null} will be displayed as -∞
     * @param max The highest allowed number. {@code null} will be displayed as ∞
     */
    public ArgumentOutOfRangeException(byte given, Byte min, Byte max) {
        this(getMessageFor(given, min, max));
    }



    private static String getMessageFor(double given, Double min, Double max, boolean startIncl, boolean endIncl) {
        boolean inverted = checkRange(given, min != null ? min : Double.NEGATIVE_INFINITY, max != null ? max : Double.POSITIVE_INFINITY);
        if(inverted) {
            Double temp = min;
            min = max;
            max = temp;
        }
        return "'" + given + "' is out of range for range " +
                (inverted ? (startIncl ? ']' : ')') : (startIncl ? '[' : '(')) +
                (min != null && min != Double.NEGATIVE_INFINITY ? min : "-\u221e")
                + ".." +
                (max != null && max != Double.POSITIVE_INFINITY ? max : '\u221e')
                + (inverted ? (endIncl ? '[' : '(') : (endIncl ? ']' : ')'));
    }

    private static String getMessageFor(float given, Float min, Float max, boolean startIncl, boolean endIncl) {
        boolean inverted = checkRange(given, min != null ? min : Float.NEGATIVE_INFINITY, max != null ? max : Float.POSITIVE_INFINITY);
        if(inverted) {
            Float temp = min;
            min = max;
            max = temp;
        }
        return "'" + given + "' is out of range for range " +
                (inverted ? (startIncl ? ']' : ')') : (startIncl ? '[' : '(')) +
                (min != null && min != Float.NEGATIVE_INFINITY ? min : "-\u221e")
                + ".." +
                (max != null && max != Float.POSITIVE_INFINITY ? max : '\u221e')
                + (inverted ? (endIncl ? '[' : '(') : (endIncl ? ']' : ')'));
    }

    private static String getMessageFor(long given, Long min, Long max) {
        boolean inverted = checkRange(given, min != null ? min : Long.MIN_VALUE, max != null ? max : Long.MAX_VALUE);
        if(inverted) {
            Long temp = min;
            min = max;
            max = temp;
        }
        return "'" + given + "' is out of range for range " +
                (inverted ? ']' : '[') +
                (min != null && min != Long.MIN_VALUE ? min : "-\u221e")
                + ".." +
                (max != null && max != Long.MAX_VALUE ? max : '\u221e')
                + (inverted ? '[' : ']');
    }

    private static String getMessageFor(int given, Integer min, Integer max) {
        boolean inverted = checkRange(given, min != null ? min : Integer.MIN_VALUE, max != null ? max : Integer.MAX_VALUE);
        if(inverted) {
            Integer temp = min;
            min = max;
            max = temp;
        }
        return "'" + given + "' is out of range for range " +
                (inverted ? ']' : '[') +
                (min != null && min != Integer.MIN_VALUE ? min : "-\u221e")
                + ".." +
                (max != null && max != Integer.MAX_VALUE ? max : '\u221e')
                + (inverted ? '[' : ']');
    }

    private static String getMessageFor(short given, Short min, Short max) {
        boolean inverted = checkRange(given, min != null ? min : Short.MIN_VALUE, max != null ? max : Short.MAX_VALUE);
        if(inverted) {
            Short temp = min;
            min = max;
            max = temp;
        }
        return "'" + given + "' is out of range for range " +
                (inverted ? ']' : '[') +
                (min != null && min != Short.MIN_VALUE ? min : "-\u221e")
                + ".." +
                (max != null && max != Short.MAX_VALUE ? max : '\u221e')
                + (inverted ? '[' : ']');
    }

    private static String getMessageFor(byte given, Byte min, Byte max) {
        boolean inverted = checkRange(given, min != null ? min : Byte.MIN_VALUE, max != null ? max : Byte.MAX_VALUE);
        if(inverted) {
            Byte temp = min;
            min = max;
            max = temp;
        }
        return "'" + given + "' is out of range for range " +
                (inverted ? ']' : '[') +
                (min != null ? min : Byte.MIN_VALUE)
                + ".." +
                (max != null ? max : Byte.MAX_VALUE)
                + (inverted ? '[' : ']');
    }

    private static boolean checkRange(double given, double min, double max) {
        if(Double.isNaN(given) || Double.isNaN(min) || Double.isNaN(max)) return false;

        boolean inverted = min > max;
        if((!inverted && given > min && given < max) || (inverted && (given < max || given > min)))
            throw new IllegalArgumentException("Exception while generating ArgumentOutOfBoundsException: '" + given + "' is not out of range for range " +
                    (inverted ? ']' : '[') +
                    (max != Double.POSITIVE_INFINITY ? max : '\u221e')
                    + ".." +
                    (min != Double.NEGATIVE_INFINITY ? min : "-\u221e")
                    + (inverted ? '[' : ']'));
        return inverted;
    }

    private static boolean checkRange(long given, long min, long max) {

        boolean inverted = min > max;
        if((!inverted && given > min && given < max) || (inverted && (given < max || given > min)))
            throw new IllegalArgumentException("Exception while generating ArgumentOutOfBoundsException: '" + given + "' is not out of range for range " +
                    (inverted ? ']' : '[') +
                    (max != Long.MAX_VALUE ? max : '\u221e')
                    + ".." +
                    (min != Long.MIN_VALUE ? min : "-\u221e")
                    + (inverted ? '[' : ']'));
        return inverted;
    }

    private static boolean checkRange(int given, int min, int max) {

        boolean inverted = min > max;
        if((!inverted && given > min && given < max) || (inverted && (given < max || given > min)))
            throw new IllegalArgumentException("Exception while generating ArgumentOutOfBoundsException: '" + given + "' is not out of range for range " +
                    (inverted ? ']' : '[') +
                    (max != Integer.MAX_VALUE ? max : '\u221e')
                    + ".." +
                    (min != Integer.MIN_VALUE ? min : "-\u221e")
                    + (inverted ? '[' : ']'));
        return inverted;
    }

    private static boolean checkRange(short given, short min, short max) {

        boolean inverted = min > max;
        if((!inverted && given > min && given < max) || (inverted && (given < max || given > min)))
            throw new IllegalArgumentException("Exception while generating ArgumentOutOfBoundsException: '" + given + "' is not out of range for range " +
                    (inverted ? ']' : '[') +
                    (max != Short.MAX_VALUE ? max : '\u221e')
                    + ".." +
                    (min != Short.MIN_VALUE ? min : "-\u221e")
                    + (inverted ? '[' : ']'));
        return inverted;
    }

    private static boolean checkRange(byte given, byte min, byte max) {

        boolean inverted = min > max;
        if((!inverted && given > min && given < max) || (inverted && (given < max || given > min)))
            throw new IllegalArgumentException("Exception while generating ArgumentOutOfBoundsException: '" + given + "' is not out of range for range " +
                    (inverted ? ']' : '[') + max + ".." + min + (inverted ? '[' : ']'));
        return inverted;
    }
}
