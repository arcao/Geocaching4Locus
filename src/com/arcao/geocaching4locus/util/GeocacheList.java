package com.arcao.geocaching4locus.util;

import geocaching.api.data.Geocache;
import geocaching.api.data.SimpleGeocache;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

public class GeocacheList extends ArrayList<SimpleGeocache> {
	private static final long serialVersionUID = 3552245571534314091L;
	
	private static final int VERSION = 1;
	private static final int TYPE_SIMPLE_GEOCACHE = 0;
	private static final int TYPE_GEOCACHE = 1;
	
	public void save(String filename) throws IOException {
		FileOutputStream os = null;
		DataOutputStream dos = null;
		try {
			 os = new FileOutputStream(filename);
			 dos = new DataOutputStream(os);
			 
			 dos.writeInt(VERSION);
			 
			 dos.writeInt(size());
			 for (int i = 0; i < size(); i++) {
				 SimpleGeocache cache = get(i);
				 dos.writeInt(cache instanceof Geocache ? TYPE_GEOCACHE : TYPE_SIMPLE_GEOCACHE);
				 cache.store(dos);
			 }
		} finally {
			if (dos != null)
				try { dos.close(); } catch (IOException e) {}
			if (os != null)
				try { os.close(); } catch (IOException e) {}
		}
	}
	
	public void load(String filename) throws IOException {
		FileInputStream is = null;
		DataInputStream dis = null;
		try {
			is = new FileInputStream(filename);
			dis = new DataInputStream(is);
			
			// version
			if (dis.readInt() != VERSION)
				throw new IOException("Wrong file version.");
			
			int count = dis.readInt();
			
			for (int i = 0; i < count; i++) {
				int type = dis.readInt();
				switch (type) {
					case TYPE_SIMPLE_GEOCACHE:
						add(SimpleGeocache.load(dis));
						break;
					case TYPE_GEOCACHE:
						add(Geocache.load(dis));
						break;
					default:
						throw new IOException("Unknown list item type");
				}
			}
		} finally {
			if (dis != null)
				try { dis.close(); } catch (IOException e) {}
			if (is != null)
				try { is.close(); } catch (IOException e) {}
		}
	}
	
	public static GeocacheList loadFrom(String filename) throws IOException {
		GeocacheList list = new GeocacheList();
		list.load(filename);
		return list;
	}
}
