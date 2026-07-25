package gr.priovolos.backend.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public final class LimitedOutputStream extends OutputStream {

    private final byte[] buffer;

    private int position;

    private boolean truncated;

    public LimitedOutputStream(int maximumBytes) {

        if (maximumBytes <= 0) {
            throw new IllegalArgumentException(
                    "Maximum output size must be positive."
            );
        }

        this.buffer = new byte[maximumBytes];
    }

    @Override
    public synchronized void write(int value) {

        if (position < buffer.length) {
            buffer[position++] = (byte) value;
        } else {
            truncated = true;
        }
    }

    @Override
    public synchronized void write(
            byte[] bytes,
            int offset,
            int length
    ) throws IOException {

        if (bytes == null) {
            throw new NullPointerException("bytes");
        }

        if (offset < 0
                || length < 0
                || offset > bytes.length - length) {
            throw new IndexOutOfBoundsException();
        }

        int available = buffer.length - position;
        int amountToCopy = Math.min(available, length);

        if (amountToCopy > 0) {
            System.arraycopy(
                    bytes,
                    offset,
                    buffer,
                    position,
                    amountToCopy
            );

            position += amountToCopy;
        }

        if (amountToCopy < length) {
            truncated = true;
        }
    }

    public synchronized String asString() {

        String result = new String(
                buffer,
                0,
                position,
                StandardCharsets.UTF_8
        );

        if (!truncated) {
            return result;
        }

        return result
                + System.lineSeparator()
                + "[OUTPUT TRUNCATED]";
    }
}
