package org.acra;

import java.lang.reflect.Field;

import org.acra.ErrorReporter.ReportsSenderWorker;

import android.util.Log;

public class ErrorReporterEx {
	private static final String TAG = ErrorReporterEx.class.getName();
	
	private static CrashReportData mCrashProperties = null; 
	
	public static CrashReportData getCrashProperties() {
		if (mCrashProperties == null) {
			try {
				Field field = ErrorReporter.class.getDeclaredField("mCrashProperties");
				mCrashProperties = (CrashReportData) field.get(null);
			} catch (Exception e) {
				Log.e(TAG, e.getMessage(), e);
			}
		}
		
		return mCrashProperties;
	}
	
	public static void storeUserComment(String comment) {
		CrashReportData data = getCrashProperties();
		if (data != null) {
			data.setProperty(ReportField.USER_COMMENT, comment);
		}
	}
	
	/**
     * Try to send a report, if an error occurs stores a report file for a later attempt. You can set the
     * {@link ReportingInteractionMode} for this specific report. Use {@link ErrorReporter#handleException(Throwable)} to use the
     * Application default interaction mode.
     * 
     * @param e
     *            The Throwable to be reported. If null the report will contain a new
     *            Exception("Report requested by developer").
     * @param reportingInteractionMode
     *            The desired interaction mode.
     */
    public static ReportsSenderWorker handleException(Throwable e, ReportingInteractionMode reportingInteractionMode) {
		return ErrorReporter.getInstance().handleException(e, reportingInteractionMode);
	}
}
