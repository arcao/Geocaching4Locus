package com.arcao.geocaching4locus.service;

import java.util.Date;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.text.Html;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.exception.ExceptionHandler;

public abstract class AbstractService extends IntentService {
	protected String TAG;
	
	public static final String ACTION_PROGRESS_UPDATE = "com.arcao.geocaching4locus.intent.action.PROGRESS_UPDATE";
	public static final String ACTION_PROGRESS_COMPLETE = "com.arcao.geocaching4locus.intent.action.PROGRESS_COMPLETE";
	
	public static final String PARAM_COUNT = "COUNT";
	public static final String PARAM_CURRENT = "CURRENT";

	private boolean canceled;
	
	protected NotificationManager notificationManager;

	protected int notificationId;
	protected int actionTextId;
	
	public AbstractService(String tag, int notificationId, int actionTextId) {
		super(tag);
		this.TAG = tag;
		this.notificationId = notificationId;
		this.actionTextId = actionTextId;
	}
	
	protected abstract void setInstance();
	protected abstract void removeInstance();
	
	protected abstract Intent createOngoingEventIntent();
	protected abstract void run(Intent intent) throws Exception;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		setInstance();
		notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
			
		canceled = false;

		startForeground(notificationId, createProgressNotification(0, 0));
	}

	protected Notification createProgressNotification(int count, int current) {
		
		Intent intent = createOngoingEventIntent();
		if (intent != null)
			intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);

		NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
		
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setOngoing(true);
		nb.setWhen(0); // this fix redraw issue while refreshing
		
		int percent = 0;
		if (count > 0)
			percent = ((current * 100) / count);
		
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
			// extract colors and text sizes for notification
			extractColors();

			RemoteViews contentView = new RemoteViews(getPackageName(), R.layout.notification_download);
			contentView.setTextViewText(R.id.progress_title, getText(actionTextId));
			
			// correct size and color
			contentView.setTextColor(R.id.progress_title, notification_title_color);
			contentView.setFloat(R.id.progress_title, "setTextSize", notification_title_size);
			contentView.setTextColor(R.id.progress_text, notification_text_color);
			
			if (count <= 0) {
				contentView.setProgressBar(R.id.progress_bar, 0, 0, true);
			} else {
				contentView.setProgressBar(R.id.progress_bar, count, current, false);
			}
			contentView.setTextViewText(R.id.progress_text, percent + "%");

			nb.setContent(contentView);
		} else {
			if (count <= 0) {
				nb.setProgress(0, 0, true);
			} else {
				nb.setProgress(count, current, false);
				nb.setContentText(String.format("%d / %d (%d%%)", current, count, percent));
			}
			
			nb.setContentTitle(getText(actionTextId));
		}

		
		nb.setContentIntent(PendingIntent.getActivity(getBaseContext(), 0, intent, 0));
		return nb.build();
	}
	
	protected Notification createErrorNotification(Intent errorIntent) {
		NotificationCompat.Builder nb = new NotificationCompat.Builder(this);
		
		final int resErrorId = errorIntent.getIntExtra(ErrorActivity.PARAM_RESOURCE_ID, 0);
		final String additionalMessage = errorIntent.getStringExtra(ErrorActivity.PARAM_ADDITIONAL_MESSAGE);
		
		nb.setSmallIcon(R.drawable.ic_launcher);
		nb.setOngoing(false);
		nb.setWhen(new Date().getTime());
		nb.setTicker(getText(R.string.error_title));
		nb.setContentTitle(getText(R.string.error_title));
		if (resErrorId != 0)
			nb.setContentText(Html.fromHtml(getString(resErrorId, additionalMessage)));

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
		canceled = true;
		removeInstance();
		stopForeground(true);
		super.onDestroy();
	}
	
	public boolean isCanceled() {
		return canceled;
	}

	protected abstract void loadConfiguration(SharedPreferences prefs);
		
	public void sendProgressUpdate(int current, int count) {
		if (canceled)
			return;
			
		notificationManager.notify(notificationId, createProgressNotification(count, current));
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_UPDATE);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, current);
		
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
	
	protected void sendProgressComplete(int count) {
		if (!canceled) {
			notificationManager.cancel(notificationId);
		}
		
		Intent broadcastIntent = new Intent();
		broadcastIntent.setAction(ACTION_PROGRESS_COMPLETE);
		broadcastIntent.putExtra(PARAM_COUNT, count);
		broadcastIntent.putExtra(PARAM_CURRENT, count);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcastIntent);
	}
	
	protected void sendError(Throwable exception) {
		Intent intent = new ExceptionHandler(this).handle(exception);
		final int resErrorId = intent.getIntExtra(ErrorActivity.PARAM_RESOURCE_ID, 0);
		
		// error notification
		notificationManager.notify(resErrorId, createErrorNotification(intent));
		LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
	}

	// --------------------- Methods to get right color and text size for title and text for notification ------------------
	protected Integer notification_title_color = null;
	protected float notification_title_size = 11;
	protected Integer notification_text_color = null;
	protected float notification_text_size = 11;
	private final String COLOR_SEARCH_RECURSE_TITLE_TIP = "SOME_SAMPLE_TITLE";
	private final String COLOR_SEARCH_RECURSE_TEXT_TIP = "SOME_SAMPLE_TEXT";

	protected boolean recurseGroup(ViewGroup gp) {
		final int count = gp.getChildCount();

		for (int i = 0; i < count; ++i) {
			if (gp.getChildAt(i) instanceof TextView) {
				final TextView text = (TextView) gp.getChildAt(i);
				final String szText = text.getText().toString();

				if (COLOR_SEARCH_RECURSE_TITLE_TIP.equals(szText)) {
					notification_title_color = text.getTextColors().getDefaultColor();
					notification_title_size = text.getTextSize();
					DisplayMetrics metrics = new DisplayMetrics();
					WindowManager systemWM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
					systemWM.getDefaultDisplay().getMetrics(metrics);
					notification_title_size /= metrics.scaledDensity;

					if (notification_title_color != null && notification_text_color != null)
						return true;

				} else if (COLOR_SEARCH_RECURSE_TEXT_TIP.equals(szText)) {
					notification_text_color = text.getTextColors().getDefaultColor();
					notification_text_size = text.getTextSize();
					DisplayMetrics metrics = new DisplayMetrics();
					WindowManager systemWM = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
					systemWM.getDefaultDisplay().getMetrics(metrics);
					notification_text_size /= metrics.scaledDensity;

					if (notification_title_color != null && notification_text_color != null)
						return true;
				}
			} else if (gp.getChildAt(i) instanceof ViewGroup) {
				if (recurseGroup((ViewGroup) gp.getChildAt(i)))
					return true;
			}
		}
		return false;
	}

	@SuppressWarnings("deprecation")
	protected void extractColors() {
		if (notification_title_color != null && notification_text_color != null)
			return;

		try {
			Notification ntf = new Notification();
			ntf.setLatestEventInfo(this, COLOR_SEARCH_RECURSE_TITLE_TIP, COLOR_SEARCH_RECURSE_TEXT_TIP, null);
			LinearLayout group = new LinearLayout(this);
			ViewGroup event = (ViewGroup) ntf.contentView.apply(this, group);
			recurseGroup(event);
			group.removeAllViews();
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
		
		if (notification_title_color == null)
			notification_title_color = android.R.color.black;
		if (notification_text_color == null)
			notification_text_color = android.R.color.black;
	}

}
