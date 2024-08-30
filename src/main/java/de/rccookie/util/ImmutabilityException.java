package de.rccookie.util;

/**
 * A subclass of {@link UnsupportedOperationException} that indicates that
 * some operation is not possible because the object that may be modified
 * is immutable.
 */
public class ImmutabilityException extends UnsupportedOperationException {

    /**
     * Creates a new immutability exception with the message
     * <code>Object is immutable</code>.
     */
    public ImmutabilityException() {
        super("Object is immutable");
    }

    /**
     * Creates a new immutability exception with a detail message.
     *
     * @param immutableObject The object that was attempted to be modified
     *                        but is immutable
     */
    public ImmutabilityException(Object immutableObject) {
        super("'" + immutableObject + "' is immutable");
    }
}
