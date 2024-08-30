package de.rccookie.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class XORDecodedInputStream extends InputStream {

    private final InputStream in;

    private long length = 0;
    private int index = 0;
    private int type = -1;
    private int[] key = null;

    public XORDecodedInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        if(length-- > 0) {
            if(type == 1)
                return in.read() ^ key[index++ & 3];
            if(type == 8) {
                in.readNBytes((int) length+1);
                length = 0;
                return -1;
            }
            return in.read() ^ key[index++ & 3];
//            throw new IllegalArgumentException("Type " + type);
        }
        type = in.read() - 128;
        if(type == -129) return -1;
        length = in.read() - 128;
        if(length == 126)
            length = (in.read() << 8) | in.read();
        else if(length == 127) {
            length = ((long) in.read() << 56) | ((long) in.read() << 48) |
                     ((long) in.read() << 40) | ((long) in.read() << 32) |
                     ((long) in.read() << 24) | ((long) in.read() << 16) |
                     ((long) in.read() << 8 ) |  (long) in.read();
        }
        key = new int[] { in.read(), in.read(), in.read(), in.read() };
        index = 0;
        return read();
    }

    @Override
    public long transferTo(OutputStream out) throws IOException {
        Arguments.checkNull(out, "out");
        long transferred = 0;
        int b;
        while ((b = read()) >= 0) {
            out.write(b);
            out.flush();
            transferred++;
        }
        return transferred;
    }
}
