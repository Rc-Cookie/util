package de.rccookie.util;

import java.util.Iterator;

public abstract class RecursiveStepIterator<T> extends StepIterator<T> {

    protected Iterator<T> it = IterableIterator.empty();
}
