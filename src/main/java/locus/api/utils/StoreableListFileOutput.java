package locus.api.utils;

import java.io.Closeable;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import locus.api.objects.Storable;

public class StoreableListFileOutput implements Closeable {
	protected boolean listOpened = false;
	protected int count = 0;
	protected int counterPosition = 0;

	protected final FileOutputStream out;
	protected final DataFileWriterBigEndian writer;

	public StoreableListFileOutput(FileOutputStream out) {
		this.out = out;
		this.writer = new DataFileWriterBigEndian(out);
	}

	public synchronized void beginList() throws IOException {
		try {
			if (listOpened)
				return;

			listOpened = true;
			count = 0;

			counterPosition = writer.getPosition();
			writer.writeInt(count);
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	public synchronized void endList() throws IOException {
		try {
			if (!listOpened)
				throw new IOException("List file structure is not prepared. Call beginList method first.");

			int lastPosition = writer.getPosition();

			writer.moveTo(counterPosition);
			writer.writeInt(count);
			writer.moveTo(lastPosition);

			count = 0;
			listOpened = false;
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	public synchronized void write(Storable obj) throws IOException {
		try {
			obj.write(writer);
			count++;
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

	public synchronized void writeCollection(final Collection<? extends Storable> objs) throws IOException {
		for(Storable obj : objs) {
			write(obj);
		}
	}

	@Override
	public void close() throws IOException {
		try {
			if (listOpened)
				endList();

			writer.flush();
			writer.close();
		} catch (DataFileWriterBigEndian.DataFileWriterException e) {
			throw e.getCause();
		}
	}

}
