package locus.api.utils;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

public class DataFileWriterBigEndian extends DataWriterBigEndian {
	protected int count = 0;
	protected long storedPosition = 0;

	protected OutputStream out;
	protected FileChannel channel;

	public DataFileWriterBigEndian(FileOutputStream out) {
		super(0);
		this.out = new BufferedOutputStream(out);
		channel = out.getChannel();
	}

	@Override
	public synchronized void write(int b) {
		try {
			out.write(b);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public synchronized void write(byte[] b, int off, int len) {
		try {
			out.write(b, off, len);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public void storePosition() {
		try {
			out.flush();
			storedPosition = channel.position();
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public void restorePosition() {
		try {
			out.flush();
			channel.position(storedPosition);
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	@Override
	public void moveTo(int index) {
		try {
			out.flush();

			// check index
			if (index < 0 || index > channel.size()) {
				throw new IllegalArgumentException(
								"Invalid move index:" + index + ", count:" + channel.size());
			}

			channel.position(index);
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
			out.flush();
			return (int) channel.size();
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	public synchronized int getPosition() {
		try {
			out.flush();
			return (int) channel.position();
		} catch (IOException e) {
			throw new DataFileWriterException(e);
		}
	}

	public synchronized void flush() throws IOException {
		out.flush();
	}

	public synchronized void close() throws IOException {
		out.close();
	}

	public static class DataFileWriterException extends RuntimeException {
		public DataFileWriterException(IOException throwable) {
			super(throwable);
		}

		@Override
		public IOException getCause() {
			return (IOException) super.getCause();
		}
	}
}
