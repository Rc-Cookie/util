package de.rccookie.util;

/**
 * Thrown to indicate that an object is immutable because it is a view
 * of some other object. The "other object" may or may not be immutable.
 */
public class ViewModificationException extends ImmutabilityException {

    /**
     * Creates a new view modification exception with the message
     * <code>Object is a view and thus immutable</code>.
     */
    public ViewModificationException() {
        super("Object is a view and thus immutable");
    }

    /**
     * Creates a new view modification exception with a detail message.
     *
     * @param viewObject The object that was attempted to be modified
     *                   but is a view of some other object
     */
    public ViewModificationException(Object viewObject) {
        super("'" + viewObject + "' is a view and thus immutable");
    }
}
