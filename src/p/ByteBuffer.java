package p;

import static java.util.Arrays.copyOf;

public class ByteBuffer {

    byte[] bytes;
    int chunkSize = 32;
    int mark = 0;

    public void append(byte b) {
        ensureAdditionalSpace(1);
        bytes[mark++] = b;
    }

    public void append(byte[] bs) {
        ensureAdditionalSpace(bs.length);

        System.arraycopy(bs, 0, bytes, mark, bs.length);
        mark += bs.length;
    }

    protected void ensureAdditionalSpace(int space) {
        if (bytes == null) {
            bytes = new byte[nextSize(space)];
        } else {
            if (getBufferSize() < space + mark) {
                bytes = copyOf(bytes, nextSize(space));
            }
        }
    }

    private int nextSize(int space) {
        return (space < chunkSize ? chunkSize : space) + mark;
    }

    private int getBufferSize() {
        return bytes == null ? 0 : bytes.length;
    }

    public byte[] toArray() {
        return copyOf(bytes, mark);
    }

    public String toString() {
        return new String(toArray());
    }

    public void reset() {
        mark = 0;
    }

    public int length() {
        return mark;
    }

    public boolean isEmpty() {
        return mark == 0;
    }

    public boolean isNotEmpty() {
        return mark > 0;
    }
}
