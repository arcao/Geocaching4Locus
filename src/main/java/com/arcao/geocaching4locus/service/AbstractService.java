package com.arcao.geocaching4locus.service;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.util.SpannedFix;

import java.util.Date;

public abstract class AbstractService extends IntentService {
	public static final String ACTION_PROGRESS_UPDATE = "com.arcao.geocaching4locus.intent.action.PROGRESS_UPDATE";
	public static final String ACTION_PROGRESS_COMPLETE = "com.arcao.geocaching4locus.intent.action.PROGRESS_COMPLETE";

	public static final String PARAM_COUNT = "COUNT";
	public static final String PARAM_CURRENT = "CURRENT";

	private boolean mCanceled;
	private NotificationManager mNotificationManager;
	private final int mNotificationId;
	private final int mActionTextId;

	public AbstractService(String tag, int notificationId, int actionTextId) {
		super(tag);

		mNotificationId = notificationId;
		mActionTextId = actionTextId;
	}

	protected abstract void setInstance();
	protected abstract void removeInstance();

	protected abstract Intent createOngoingEventIntent();
	protected abstract void run(Intent intent) throws Exception;

	@Override
	public void onCreate() {
		super.onCreate();

		setInstance();
		mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		mCanceled = false;

		startForeground(mNotificationId, createProgressNotification(0, 0));
	}

	protected Notification createProgressNotification(int count, int current) {

		Intent intent = createOngoingEventIntent();
		if (intent != null)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationCompat.Builder nb = new NotificationCompat.Builder(this);

		nb.setSmallIcon(android.R.drawable.stat_sys_download);
		nb.setOngoing(true);
		nb.setWhen(0); // this fix redraw issue while refreshing
		nb.setLocalOnly(true);
		nb.setCategory(NotificationCompat.CATEGORY_PROGRESS);

		int percent = 0;
		if (count > 0)
			percent = ((current * 100) / count);

		if (count <= 0) {
			nb.setProgress(0, 0, true);
		} else {
			nb.setProgress(count, current, false);
			nb.setContentText(String.format("%d / %d (%d%%)", current, count, percent));
		}

		nb.setContentTitle(getText(mActionTextId));

		nb.setContentIntent(PendingIntent.getActivity(getBaseContext(), 0, intent, 0));
		return nb.build();
	}

	protected Notification createErrorNotification(Intent errorIntent) {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this);

		final int resErrorId = errorIntent.getIntExtra(ErrorActivity.PARAM_RESOURCE_TEXT, 0);
		final String additionalMessage = errorIntent.getStringExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE);

		nb.setSmallIcon(android.R.drawable.stat_sys_warning);
		nb.setOngoing(false);
		nb.setWhen(new Date().getTime());
		nb.setTicker(getText(R.string.error_title));
		nb.setContentTitle(getText(R.string.error_title));
		if (resErrorId != 0)
			nb.setContentText(SpannedFix.fromHtml(getString(resErrorId, additionalMessage)));

		Intent intent = new Intent(errorIntent);
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
		nb.setContentIntent(PendingIntent.getActivity(getBaseContext(), 0, intent, 0));

		return nb.build();
	}

	@Override
	protected void onHandleIntent(Intent intent) {
		loadConfiguration(PreferenceManager.getDefaultSharedPreferences(this));

		try {
			run(intent);
		} catch (Exception e) {
			sendError(e);
		}
	}

	@Override
	public void onDestroy() {
		mCanceled = true;
		removeInstance();
		stopForeground(true);
		super.onDestroy();
	}

	public boolean isCanceled() {
		return mCanceled;
	}

	protected abstract void loadConfiguration(SharedPreferences prefs);

	public void sendProgressUpdate(int current, int count) {
		if (mCanceled)
			return;

		mNotificationManager.notify(mNotificationId, createProgressNotification(count, current));

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_UPDATE);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, current);

		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

	protected void sendProgressComplete(int count) {
		mNotificationManager.cancel(mNotificationId);

		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_COMPLETE);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, count);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}

	protected void sendError(Throwable exception) {
		mNotificationManager.cancel(mNotificationId);

		Intent intent = new ExceptionHandler(this).handle(exception);
		final int resErrorId = intent.getIntExtra(ErrorActivity.PARAM_RESOURCE_TEXT, 0);

		// error notification
		mNotificationManager.notify(resErrorId, createErrorNotification(intent));
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}
}
