package com.github.rccookie.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Random;

public class XOREncodedOutputStream extends OutputStream {

    private final OutputStream out;
    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final Random random = new Random();

    private boolean closed = false;

    public XOREncodedOutputStream(OutputStream out) {
        this.out = out;
    }

    @Override
    public void write(int b) throws IOException {
        if(closed) throw new IllegalStateException();
        buffer.write(b);
    }

    @Override
    public void flush() throws IOException {
        byte[] bytes = buffer.toByteArray();
        buffer.reset();

        out.write(1 + 128); // type
        int length = bytes.length;
        if(length < 126)
            out.write(length + 128);
        else if(length < (1 << 16)) {
            out.write(126 + 128);
            out.write((length >> 8) & 0xFF);
            out.write(length & 0xFF);
        }
        else {
            out.write(127 + 128);
            out.write((length >> 24) & 0xFF);
            out.write((length >> 16) & 0xFF);
            out.write((length >> 8) & 0xFF);
            out.write(length & 0xFF);
        }
        int r = random.nextInt();
        int[] key = { (r >> 24) & 0xFF, (r >> 16) & 0xFF, (r >> 8) & 0xFF, r & 0xFF };
        for(int i=0; i<4; i++)
            out.write(key[i]);
        for(int i=0; i<length; i++)
            out.write((bytes[i]) ^ key[i & 3]);

        super.flush();
    }

    @Override
    public void close() throws IOException {
        out.close();
        closed = true;
        super.close();
    }
}
