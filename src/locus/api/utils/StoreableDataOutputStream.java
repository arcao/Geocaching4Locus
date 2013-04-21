package locus.api.utils;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Collection;

import locus.api.objects.Storable;

public class StoreableDataOutputStream extends DataOutputStream {
	protected boolean listOpened = false;
	protected int count = 0;
	protected long countPosition = 0;

	protected FileOutputStream out;

	public StoreableDataOutputStream(FileOutputStream out) {
		super(new BufferedOutputStream(out));
		this.out = out;
	}

	public synchronized void beginList() throws IOException {
		if (listOpened)
			return;

		listOpened = true;
		count = 0;

		flush();
		countPosition = out.getChannel().position();
		writeInt(0);
	}

	public synchronized void endList() throws IOException {
		if (!listOpened)
			throw new IOException("List file structure is not prepared. Call beginList method first.");

		flush();
		long lastPosition = out.getChannel().position();

		out.getChannel().position(countPosition);
		writeInt(count);
		flush();

		out.getChannel().position(lastPosition);

		count = 0;
		listOpened = false;
	}

	public synchronized void write(Storable obj) throws IOException {
		obj.write(this);
		count++;
	}

	public synchronized void writeCollection(final Collection<? extends Storable> objs) throws IOException {
		for(Storable obj : objs) {
			write(obj);
		}
	}

	@Override
	public void close() throws IOException {
		if (listOpened)
			endList();

		super.close();
	}

}
