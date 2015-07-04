package com.arcao.feedback;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.provider.OpenableColumns;
import android.webkit.MimeTypeMap;

import com.arcao.geocaching4locus.BuildConfig;

import java.io.File;
import java.io.FileNotFoundException;

import timber.log.Timber;

/**
 * <p>
 *   Provider for attaching feedback file to 3rd application:
 * </p>
 * <p>
 *   Usage in AndroidManifest.xml file:
 * </p>
 *
 * <pre class="prettyprint">
 * &lt;provider
 *   android:name="com.arcao.feedback.FeedbackFileProvider"
 *   android:authorities="${applicationId}.provider.feedback"
 *   android:exported="true"
 *   android:enabled="true"
 *   android:grantUriPermissions="true"/&gt;
 * </pre>
 */
public class FeedbackFileProvider extends ContentProvider {
	private static final String[] COLUMNS = { OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE };
	private static final String AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.feedback";
	private static final String REPORT_FILE_NAME = "logs.zip";
	private static final int REPORT_FILE_ID = 1;

	// UriMatcher used to match against incoming requests
	private UriMatcher uriMatcher;

	public static File getReportFile(Context context) {
		return new File(context.getCacheDir(), REPORT_FILE_NAME);
	}

	public static Uri getReportFileUri() {
		return new Uri.Builder().scheme("content").authority(AUTHORITY).path(REPORT_FILE_NAME).build();
	}

	@Override
	public boolean onCreate() {
		uriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
		uriMatcher.addURI(AUTHORITY, REPORT_FILE_NAME, REPORT_FILE_ID);

		return true;
	}

	@Override
	public ParcelFileDescriptor openFile(Uri uri, String mode) throws FileNotFoundException {
		Timber.v("openFile: Called with uri: '" + uri + "'.");

		// Check incoming Uri against the matcher
		switch (uriMatcher.match(uri)) {
			case REPORT_FILE_ID:
				File reportFile = getReportFile(getContext());

				if (!reportFile.exists()) {
					Timber.e("File '" + reportFile +"' for uri '" + uri +"' not found");
					throw new FileNotFoundException(reportFile.toString());
				}

				return ParcelFileDescriptor.open(getReportFile(getContext()), ParcelFileDescriptor.MODE_READ_ONLY);

			default:
				Timber.e("Unsupported uri: '" + uri + "'.");
				throw new FileNotFoundException("Unsupported uri: "	+ uri.toString());
		}
	}

	@Override
	public String getType(Uri uri) {
		Timber.v("getType: Called with uri: '" + uri + "'");

		String fileName = uri.getLastPathSegment();

		final int lastDot = fileName.lastIndexOf('.');
		if (lastDot >= 0) {
			final String extension = fileName.substring(lastDot + 1);
			final String mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
			if (mime != null) {
				return mime;
			}
		}

		return "application/octet-stream";
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String s, String[] as1,	String s1) {
		switch (uriMatcher.match(uri)) {
			case REPORT_FILE_ID:
				final File file = getReportFile(getContext());

				if (projection == null) {
					projection = COLUMNS;
				}

				String[] cols = new String[projection.length];
				Object[] values = new Object[projection.length];
				int i = 0;
				for (String col : projection) {
					if (OpenableColumns.DISPLAY_NAME.equals(col)) {
						cols[i] = OpenableColumns.DISPLAY_NAME;
						values[i++] = file.getName();
					} else if (OpenableColumns.SIZE.equals(col)) {
						cols[i] = OpenableColumns.SIZE;
						values[i++] = file.length();
					}
				}

				cols = copyOf(cols, i);
				values = copyOf(values, i);

				final MatrixCursor cursor = new MatrixCursor(cols, 1);
				cursor.addRow(values);
				return cursor;
			default:
				return null;
		}
	}

	private static String[] copyOf(String[] original, int newLength) {
		final String[] result = new String[newLength];
		System.arraycopy(original, 0, result, 0, newLength);
		return result;
	}

	private static Object[] copyOf(Object[] original, int newLength) {
		final Object[] result = new Object[newLength];
		System.arraycopy(original, 0, result, 0, newLength);
		return result;
	}

	// Not supported / used / methods
	@Override
	public int update(Uri uri, ContentValues contentvalues, String s,	String[] as) {
		throw new UnsupportedOperationException("No external updates");
	}

	@Override
	public int delete(Uri uri, String s, String[] as) {
		throw new UnsupportedOperationException("No external deletes");
	}

	@Override
	public Uri insert(Uri uri, ContentValues contentvalues) {
		throw new UnsupportedOperationException("No external inserts");
	}
}
