package locus.api.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

import locus.api.objects.Storable;

public class StoreableWriter implements Closeable {
    private boolean closed;
    private int count;
    private final int counterPosition;

    private final DataFileWriterBigEndian writer;

    public StoreableWriter(FileOutputStream out) throws IOException {
        try {
            count = 0;
            writer = new DataFileWriterBigEndian(out);
            counterPosition = writer.getPosition();
            writer.writeInt(count);
        } catch (DataFileWriterBigEndian.DataFileWriterException e) {
            throw e.getCause();
        }

    }

    /**
     * Return count of written Storeable objects
     *
     * @return count of written Storeable objects
     */
    public int getSize() {
        return count;
    }

    public synchronized void write(Storable obj) throws IOException {
        if (closed)
            throw new IOException("Output stream closed.");

        try {
            obj.write(writer);
            count++;
        } catch (DataFileWriterBigEndian.DataFileWriterException e) {
            throw e.getCause();
        }
    }

    @Override
    public synchronized void close() throws IOException {
        try {
            if (closed)
                throw new IOException("Output stream already closed.");

            closed = true;

            int lastPosition = writer.getPosition();

            writer.moveTo(counterPosition);
            writer.writeInt(count);
            writer.moveTo(lastPosition);

            writer.flush();
            writer.close();
        } catch (DataFileWriterBigEndian.DataFileWriterException e) {
            throw e.getCause();
        }
    }
}
