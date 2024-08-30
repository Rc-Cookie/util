package de.rccookie.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.jetbrains.annotations.NotNull;

public class Pipe extends InputStream {

    private final PipedInputStream in;
    private final PipedOutputStream out;

    public Pipe(int bufferSize) throws IOException {
        in = new PipedInputStream(bufferSize);
        out = new PipedOutputStream(in);
    }

    public Pipe() throws IOException {
        in = new PipedInputStream();
        out = new PipedOutputStream(in);
    }

    public OutputStream out() {
        return out;
    }

    @Override
    public int read() throws IOException {
        return in.read();
    }

    @Override
    public int read(byte @NotNull [] b) throws IOException {
        return in.read(b);
    }

    @Override
    public int read(byte @NotNull[] b, int off, int len) throws IOException {
        return in.read(b, off, len);
    }

    @Override
    public byte[] readAllBytes() throws IOException {
        return in.readAllBytes();
    }

    @Override
    public byte[] readNBytes(int len) throws IOException {
        return in.readNBytes(len);
    }

    @Override
    public int readNBytes(byte[] b, int off, int len) throws IOException {
        return in.readNBytes(b, off, len);
    }

    @Override
    public long skip(long n) throws IOException {
        return in.skip(n);
    }

    @Override
    public int available() throws IOException {
        return in.available();
    }

    @Override
    public void close() throws IOException {
        in.close();
    }

    @Override
    public synchronized void mark(int readLimit) {
        in.mark(readLimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        in.reset();
    }

    @Override
    public boolean markSupported() {
        return in.markSupported();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        return in.transferTo(out);
    }

    public void write(int b) throws IOException {
        out.write(b);
    }

    public void write(byte @NotNull [] b) throws IOException {
        out.write(b);
    }

    public void write(byte @NotNull[] b, int off, int len) throws IOException {
        out.write(b, off, len);
    }

    public void flush() throws IOException {
        out.flush();
    }
}
