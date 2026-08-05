package gr.priovolos.backend.ssh;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Output stream implementation that stores a fixed maximum number
 * of bytes in memory.
 *
 * <p>This class is used to safely capture SSH command output while
 * preventing excessive memory consumption caused by unexpectedly
 * large responses from remote network devices.</p>
 *
 * <p>Once the configured capacity has been reached, additional data
 * is discarded and the stream is marked as truncated. When the
 * captured output is later converted to a string, a truncation
 * indicator is appended to inform the caller that part of the
 * command output has been omitted.</p>
 *
 * <p>The implementation is thread-safe by synchronizing all write
 * operations and output retrieval.</p>
 *
 * @author Ioannis Priovolos
 */
public final class LimitedOutputStream extends OutputStream {

    private final byte[] buffer;

    private int position;

    private boolean truncated;

    /**
     * Creates a new output stream with the specified maximum
     * capacity.
     *
     * @param maximumBytes the maximum number of bytes that may be
     *                     stored
     * @throws IllegalArgumentException if the supplied capacity is
     *                                  zero or negative
     */
    public LimitedOutputStream(int maximumBytes) {

        if (maximumBytes <= 0) {
            throw new IllegalArgumentException(
                    "Maximum output size must be positive."
            );
        }

        this.buffer = new byte[maximumBytes];
    }

    /**
     * Writes a single byte to the stream.
     *
     * <p>If the internal buffer is already full, the byte is
     * discarded and the stream is marked as truncated.</p>
     *
     * @param value the byte to write
     */
    @Override
    public synchronized void write(int value) {

        if (position < buffer.length) {
            buffer[position++] = (byte) value;
        } else {
            truncated = true;
        }
    }

    /**
     * Writes a sequence of bytes to the stream.
     *
     * <p>If the supplied data exceeds the remaining buffer capacity,
     * only the bytes that fit are stored. The remaining bytes are
     * discarded and the stream is marked as truncated.</p>
     *
     * @param bytes the source byte array
     * @param offset the starting offset within the array
     * @param length the number of bytes to write
     * @throws NullPointerException if the byte array is {@code null}
     * @throws IndexOutOfBoundsException if the supplied offset or
     *                                   length is invalid
     */
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

    /**
     * Returns the captured output as a UTF-8 encoded string.
     *
     * <p>If the output exceeded the configured capacity, a truncation
     * notice is appended to the returned string to indicate that not
     * all output could be retained.</p>
     *
     * @return the captured output as a string
     */
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

        return result + System.lineSeparator() + "[OUTPUT TRUNCATED]";
    }
}
