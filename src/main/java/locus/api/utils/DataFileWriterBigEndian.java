package locus.api.utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class DataFileWriterBigEndian extends DataWriterBigEndian {
	private long mStoredPosition = 0;

	private final OutputStream mOut;
	private final FileChannel mChannel;

	public DataFileWriterBigEndian(FileOutputStream out) {
		super(0);
		this.mOut = new BufferedOutputStream(out);
		mChannel = out.getChannel();
	}

	@Override
	public synchronized void write(int b) {
		try {
			mOut.write(b);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) {
		try {
			mOut.write(b, off, len);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public void storePosition() {
		try {
			mOut.flush();
			mStoredPosition = mChannel.position();
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public void restorePosition() {
		try {
			mOut.flush();
			mChannel.position(mStoredPosition);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public void moveTo(int index) {
		try {
			mOut.flush();

			// check index
			if (index < 0 || index > mChannel.size()) {
				throw new IllegalArgumentException(
								"Invalid move index:" + index + ", count:" + mChannel.size());
			}

			mChannel.position(index);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public synchronized void writeTo(OutputStream out) throws IOException {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public synchronized byte[] toByteArray() {
		throw new UnsupportedOperationException("Not supported");
	}

	@Override
	public synchronized int size() {
		try {
			mOut.flush();
			return (int) mChannel.size();
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	public synchronized int getPosition() {
		try {
			mOut.flush();
			return (int) mChannel.position();
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	public synchronized void flush() throws IOException {
		mOut.flush();
	}

	public synchronized void close() throws IOException {
		mOut.close();
	}

	public static class DataFileWriterException extends RuntimeException {
		private static final long serialVersionUID = 2678019269077897465L;

		public DataFileWriterException(IOException throwable) {
			super(throwable);
		}

		@Override
		public IOException getCause() {
			return (IOException) super.getCause();
		}
	}
}
