package de.rccookie.util;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.reflect.InvocationTargetException;

import org.jetbrains.annotations.Contract;

/**
 * A runtime exception that wraps an exception and prints the wrapped exception directly.
 */
public class UncheckedException extends RuntimeException {

    public UncheckedException(Throwable cause) {
        super(cause);
    }

    @Override
    public String toString() {
        return "Unchecked[" + super.toString() + "]";
    }

    @Override
    public void printStackTrace() {
        printStackTrace(System.err);
    }

    @Override
    public void printStackTrace(PrintStream s) {
        if(!(getCause() instanceof RuntimeException || getCause() instanceof Error))
            s.print("Unchecked ");
        getCause().printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        if(!(getCause() instanceof RuntimeException || getCause() instanceof Error))
            s.print("Unchecked ");
        getCause().printStackTrace(s);
    }


    /**
     * Wraps the given exception in an {@link UncheckedException} if it is not a {@link RuntimeException}.
     *
     * @param t The Throwable to be represented as a RuntimeException
     * @return The exception itself if it already is a RuntimeException, otherwise a new UncheckedException with t as cause. If t is null, null is returned
     */
    public static RuntimeException of(Throwable t) {
        if(t == null) return null;
        if(t instanceof RuntimeException) return (RuntimeException) t;
        return new UncheckedException(t);
    }

    /**
     * If the given exception is a {@link RuntimeException} or an {@link Error}, it throws it directly.
     * If the given exception is a {@link InvocationTargetException}, the target exception will be rethrown.
     * Otherwise, it throws an {@link UncheckedException} wrapping the given exception. If the supplied
     * exception is <code>null</code>, a {@link NullPointerException} will be thrown.
     * <p><b>This method never returns normally!</b></p>
     *
     * @param t The throwable to rethrow unchecked
     * @return Nothing. This method never returns normally. For convenience, the return type of this method
     *         is {@link RuntimeException} which allows to write <code>throw rethrow(...);</code>. This might
     *         be necessary in some cases for compilation reasons.
     */
    @Contract("_->fail")
    public static RuntimeException rethrow(Throwable t) throws RuntimeException, Error {
        Arguments.checkNull(t, "t");
        if(t instanceof RuntimeException)
            throw (RuntimeException) t;
        if(t instanceof Error)
            throw (Error) t;
        if(t instanceof InvocationTargetException)
            throw rethrow(t.getCause());
        throw new UncheckedException(t);
    }
}
