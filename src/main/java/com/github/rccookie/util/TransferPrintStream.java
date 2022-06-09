package com.github.rccookie.util;

import java.io.OutputStream;
import java.io.PrintStream;

import org.jetbrains.annotations.NotNull;

public class TransferPrintStream extends PrintStream {

    public TransferPrintStream(@NotNull OutputStream out) {
        super(new TransferOutputStream(out));
    }

    @NotNull
    public OutputStream getOut() {
        return out;
    }

    public void setOut(@NotNull OutputStream out) {
        this.out = Arguments.checkNull(out, "out");
    }
}
