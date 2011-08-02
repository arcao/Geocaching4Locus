package com.arcao.geocaching4locus.provider;

import java.util.ArrayList;

import menion.android.locus.addon.publiclib.geoData.PointsData;
import menion.android.locus.addon.publiclib.utils.DataCursor;
import menion.android.locus.addon.publiclib.utils.DataStorage;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.util.Log;

public class DataStorageProvider extends ContentProvider {

	private final static String TAG = "DataStorageProvider";
	public final static String URI = "content://" + DataStorageProvider.class.getCanonicalName().toLowerCase();
	
	@Override
	public Cursor query(Uri aUri, String[] aProjection, String aSelection,
			String[] aSelectionArgs, String aSortOrder) {
		
		DataCursor cursor = new DataCursor(new String[] {"data"});
		
		ArrayList<PointsData> data = DataStorage.getData();
		if (data == null || data.size() == 0)
			return cursor;
		
		for (int i = 0; i < data.size(); i++) {
			// get byte array
			Parcel par = Parcel.obtain();
			data.get(i).writeToParcel(par, 0);
			byte[] byteData = par.marshall();
			
			Log.i(TAG, "Adding row " + i + ", Row size: " + byteData.length);
			
			// add to row
			cursor.addRow(new Object[] {byteData});
		}
		// data filled to cursor, clear reference to prevent some memory issue
		DataStorage.clearData();
		// now finally return filled cursor
		return cursor;
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		return 0;
	}

	@Override
	public String getType(Uri uri) {
		return "vnd.android.cursor.dir/vnd.locus.pointsdata";
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		return null;
	}

	@Override
	public boolean onCreate() {
		return true;
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		return 0;
	}
	
}
