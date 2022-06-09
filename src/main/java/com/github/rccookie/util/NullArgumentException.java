package com.github.rccookie.util;

import org.jetbrains.annotations.Nullable;

/**
 * Exception thrown to indicate that an argument that was received was null
 * and should not have been. Subclass of {@link NullPointerException} so
 * it will be caught by the same catch blocks.
 */
public class NullArgumentException extends NullPointerException {

    /**
     * Constructs a new ArgumentNullException.
     *
     * @param parameterName The name of the parameter that was null. Pass
     *                      {@code null} for a generic message
     */
    public NullArgumentException(@Nullable String parameterName) {
        this(parameterName, null);
    }

    /**
     * Constructs a new ArgumentNullException.
     *
     * @param parameterName The name of the parameter that was null. Pass
     *                      {@code null} for a generic message
     * @param message The message to add to the generated message
     */
    public NullArgumentException(@Nullable String parameterName, @Nullable String message) {
        super((parameterName != null ? "'" + parameterName + "' was null" : "Argument was null")
                + (message != null ? " - " + message : ""));
    }
}
