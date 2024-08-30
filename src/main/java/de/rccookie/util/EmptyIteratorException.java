package de.rccookie.util;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class EmptyIteratorException extends NoSuchElementException {

    private static final String MESSAGE = "The iterator has no more elements to iterate over";

    public EmptyIteratorException() {
        super(MESSAGE);
    }

    public EmptyIteratorException(Iterator<?> iterator) {
        super("The iterator " + iterator + " has no more elements to iterate over");
    }
}
