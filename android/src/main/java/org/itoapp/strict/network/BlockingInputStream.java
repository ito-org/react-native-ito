package org.itoapp.strict.network;

import java.io.IOException;
import java.io.InputStream;

class BlockingInputStream extends InputStream {
    private final InputStream stream;

    public BlockingInputStream(InputStream content) {
        stream = content;
    }

    /*
       uses the implementation from <super>.read(byte b[], int off, int len)
       this will only return if array was filled to request or -1 or exception occurred
     */

    @Override
    public int read() throws IOException {
        return stream.read();
    }

    @Override
    public int available() throws IOException {
        return stream.available();
    }
}
