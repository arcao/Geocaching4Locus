package locus.api.utils;

import locus.api.objects.Storable;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;

public class StoreableListFileOutput implements Closeable {
	private boolean mListOpened = false;
	private int mCount = 0;
	private int mCounterPosition = 0;

	private final DataFileWriterBigEndian mWriter;

	public StoreableListFileOutput(FileOutputStream out) {
		this.mWriter = new DataFileWriterBigEndian(out);
	}

	public int getItemCount() {
		return mCount;
	}

	public synchronized void beginList() throws IOException {
		try {
			if (mListOpened)
				return;

			mListOpened = true;
			mCount = 0;

			mCounterPosition = mWriter.getPosition();
			mWriter.writeInt(mCount);
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	public synchronized void endList() throws IOException {
		try {
			if (!mListOpened)
				throw new IOException("List file structure is not prepared. Call beginList method first.");

			int lastPosition = mWriter.getPosition();

			mWriter.moveTo(mCounterPosition);
			mWriter.writeInt(mCount);
			mWriter.moveTo(lastPosition);

			mCount = 0;
			mListOpened = false;
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	public synchronized void write(Storable obj) throws IOException {
		try {
			obj.write(mWriter);
			mCount++;
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (mListOpened)
				endList();

			mWriter.flush();
			mWriter.close();
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}
}
