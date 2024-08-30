package de.rccookie.util;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Range;

/**
 * Exception thrown to indicate that an element of an array argument that was
 * received was null and should not have been. Subclass of {@link NullPointerException}
 * and {@link NullArgumentException} so it will be caught by the same catch blocks.
 */
public class NullArrayArgumentException extends NullArgumentException {

    /**
     * Constructs a new NullArrayArgumentException.
     *
     * @param parameterName The name of the parameter that was null. Pass
     *                      {@code null} for a generic message
     * @param array The array in which an element was null but should not have been
     * @param index The index in the array which has the null element
     * @throws NullArgumentException If the supplied array was null
     * @throws IndexOutOfBoundsException If the index is not a valid index in the array
     */
    public NullArrayArgumentException(@Nullable String parameterName, @Nullable Object @NotNull [] array, @Range(from = 0) int index) {
        this(parameterName, array, index, null);
    }

    /**
     * Constructs a new NullArrayArgumentException.
     *
     * @param parameterName The name of the parameter that was null. Pass
     *                      {@code null} for a generic message
     * @param array The array in which an element was null but should not have been
     * @param index The index in the array which has the null element
     * @param message The message to add to the generated message
     * @throws NullArgumentException If the supplied array was null
     * @throws IndexOutOfBoundsException If the index is not a valid index in the array
     */
    public NullArrayArgumentException(@Nullable String parameterName, @Nullable Object @NotNull [] array, @Range(from = 0) int index, @Nullable String message) {
        super(getMessage(parameterName, array, index, message), true);
    }

    @SuppressWarnings("ConstantConditions")
    private static String getMessage(@Nullable String parameterName, @Nullable Object @NotNull [] array, @Range(from = 0) int index, @Nullable String message) {
        Arguments.checkNull(array, parameterName);
        if(index < 0 || index >= array.length)
            throw new IndexOutOfBoundsException("Error while creating NullArrayArgumentException for " + parameterName +
                    ": specified index with null value was out of bounds (" + index + (index < 0 ? " < 0" : " >= " + array.length) + ")");

        String suffix = message != null ? " - " + message : "";
        boolean allNull = false;
        if(index == 0) {
            for (int i = 1; i < array.length; i++){
                if(array[i] == null) {
                    allNull = true;
                    break;
                }
            }
        }

        if(allNull)
            suffix = " (all array elements are null)" + suffix;

        if(parameterName != null)
            return "'" + parameterName + '[' + index + "]' was null" + suffix;
        return "Array argument was null on index " + index + suffix;
    }
}
