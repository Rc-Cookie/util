package de.rccookie.util;

public abstract class StepIterator<T> implements IterableIterator<T> {

    private T next;
    private boolean ready = false;

    @Override
    public boolean hasNext() {
        prepNext();
        return next != null;
    }

    @Override
    public T next() {
        prepNext();
        if(next == null) throw new EmptyIteratorException();
        ready = false;
        return next;
    }

    private void prepNext() {
        if(ready) return;
        next = getNext();
        ready = true;
    }

    protected abstract T getNext();
}
