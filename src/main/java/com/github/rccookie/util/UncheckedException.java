package com.github.rccookie.util;

import java.io.PrintStream;
import java.io.PrintWriter;

public class UncheckedException extends RuntimeException {

    public UncheckedException(Exception cause) {
        super(cause);
    }

    @Override
    public void printStackTrace() {
        getCause().printStackTrace();
    }

    @Override
    public void printStackTrace(PrintStream s) {
        getCause().printStackTrace(s);
    }

    @Override
    public void printStackTrace(PrintWriter s) {
        getCause().printStackTrace(s);
    }
}
