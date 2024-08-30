package de.rccookie.util;

import java.util.ArrayList;
import java.util.List;

import de.rccookie.util.function.ThrowingRunnable;
import org.jetbrains.annotations.Nullable;

public class ExceptionContainer<E extends Throwable> {

    private Throwable main = null;
    private final List<Throwable> all = new ArrayList<>();

    private void addUnchecked(Throwable t) {
        synchronized(all) {
            if(main == null)
                main = t;
            else main.addSuppressed(t);
            all.add(t);
        }
    }

    public void add(E exception) {
        addUnchecked(exception);
    }

    public void add(RuntimeException runtimeException) {
        addUnchecked(runtimeException);
    }

    public void add(Error error) {
        addUnchecked(error);
    }

    @SuppressWarnings("unchecked")
    public boolean tryRun(ThrowingRunnable<? extends E> code) {
        try {
            code.run();
            return true;
        } catch(Error e) {
            add(e);
        } catch(RuntimeException e) {
            add(e);
        } catch(Throwable e) {
            add((E) e);
        }
        return false;
    }

    public boolean hasAny() {
        return main != null;
    }

    public boolean hasNone() {
        return main == null;
    }

    public List<Throwable> getAll() {
        synchronized(all) {
            return List.copyOf(all);
        }
    }

    @Nullable
    public Throwable getCombined() {
        return main;
    }

    @SuppressWarnings("unchecked")
    public void throwIfAny() throws E {
        Throwable e = getCombined();
        if(e == null)
            return;
        if(e instanceof RuntimeException)
            throw (RuntimeException) e;
        if(e instanceof Error)
            throw (Error) e;
        throw (E) e;
    }

    public void uncheckedThrowIfAny() {
        Throwable e = getCombined();
        if(e != null)
            Utils.rethrow(e);
    }
}
