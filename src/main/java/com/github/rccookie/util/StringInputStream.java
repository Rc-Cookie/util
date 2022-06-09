package com.github.rccookie.util;

import java.io.ByteArrayInputStream;
import java.nio.charset.Charset;

public class StringInputStream extends ByteArrayInputStream {

    public StringInputStream(String str) {
        this(str, null);
    }

    public StringInputStream(String str, Charset charset) {
        super(charset == null ? str.getBytes() : str.getBytes(charset));
    }
}
