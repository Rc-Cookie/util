package de.rccookie.util;

import java.io.IOException;
import java.io.OutputStream;

import org.jetbrains.annotations.NotNull;

public class TransferOutputStream extends OutputStream {

    private OutputStream out;

    public TransferOutputStream(@NotNull OutputStream out) {
        this.out = Arguments.checkNull(out, "out");
    }

    @Override
    public void write(int b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte @NotNull [] b) throws IOException {
        out.write(b);
    }

    @Override
    public void write(byte @NotNull [] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        out.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
    }

    @NotNull
    public OutputStream getOut() {
        return out;
    }

    public void setOut(@NotNull OutputStream out) {
        this.out = Arguments.checkNull(out, "out");
    }
}
