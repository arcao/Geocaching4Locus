package locus.api.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

import locus.api.objects.Storable;

public class StoreableWriter implements Closeable {
	private boolean mClosed = false;
	private int mCount = 0;
	private int mCounterPosition = 0;

	private final DataFileWriterBigEndian mWriter;

	public StoreableWriter(FileOutputStream out) throws IOException {
		try {
			mCount = 0;
			mWriter = new DataFileWriterBigEndian(out);
			mCounterPosition = mWriter.getPosition();
			mWriter.writeInt(mCount);
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}

	}

	/**
	 * Return count of written Storeable objects
	 * @return count of written Storeable objects
     */
	public int getSize() {
		return mCount;
	}

	public synchronized void write(Storable obj) throws IOException {
		if (mClosed)
			throw new IOException("Output stream closed.");

		try {
			obj.write(mWriter);
			mCount++;
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	@Override
	public synchronized void close() throws IOException {
		try {
			if (mClosed)
				throw new IOException("Output stream already closed.");

			mClosed = true;

			int lastPosition = mWriter.getPosition();

			mWriter.moveTo(mCounterPosition);
			mWriter.writeInt(mCount);
			mWriter.moveTo(lastPosition);

			mWriter.flush();
			mWriter.close();
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}
}
