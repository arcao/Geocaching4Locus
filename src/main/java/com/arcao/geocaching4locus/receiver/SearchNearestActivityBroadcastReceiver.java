package com.arcao.geocaching4locus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.SearchNearestActivity;
import com.arcao.geocaching4locus.fragment.AbstractDialogFragment;
import com.arcao.geocaching4locus.fragment.AbstractDialogFragment.CancellableDialog;
import com.arcao.geocaching4locus.fragment.DownloadProgressDialogFragment;
import com.arcao.geocaching4locus.service.SearchGeocacheService;

import java.lang.ref.WeakReference;

public class SearchNearestActivityBroadcastReceiver extends BroadcastReceiver implements CancellableDialog {
	private static final String TAG = "G4L|SearchNearestActivityBroadcastReceiver";

	protected final WeakReference<SearchNearestActivity> activityRef;
	protected DownloadProgressDialogFragment pd;

	protected boolean registered;

	public SearchNearestActivityBroadcastReceiver(SearchNearestActivity activity) {
		activityRef = new WeakReference<>(activity);

		registered = false;
	}

	public void register(Context ctx) {
		if (registered)
			return;

		IntentFilter filter = new IntentFilter();
		filter.addAction(SearchGeocacheService.ACTION_PROGRESS_UPDATE);
		filter.addAction(SearchGeocacheService.ACTION_PROGRESS_COMPLETE);
		filter.addAction(ErrorActivity.ACTION_ERROR);

		LocalBroadcastManager.getInstance(ctx).registerReceiver(this, filter);
		Log.i(TAG, "Receiver registered.");
		registered = true;
	}

	public void unregister(Context ctx) {
		if (!registered)
			return;

		LocalBroadcastManager.getInstance(ctx).unregisterReceiver(this);

		if (pd != null)
			pd.dismiss();

		Log.i(TAG, "Receiver unregistered.");
		registered = false;
	}

	@Override
	public synchronized void onReceive(Context context, final Intent intent) {
		SearchNearestActivity activity = activityRef.get();
		if (activity == null || !registered)
			return;

		if (pd == null)
			pd = (DownloadProgressDialogFragment) activity.getSupportFragmentManager().findFragmentByTag(DownloadProgressDialogFragment.TAG);

		switch(intent.getAction()) {
			case SearchGeocacheService.ACTION_PROGRESS_UPDATE:
				if (pd == null || !pd.isShowing()) {
					pd = DownloadProgressDialogFragment.newInstance(R.string.downloading, intent.getIntExtra(SearchGeocacheService.PARAM_COUNT, 1), intent.getIntExtra(SearchGeocacheService.PARAM_CURRENT, 0));
					pd.setOnCancelListener(this);
					pd.show(activity.getSupportFragmentManager(), DownloadProgressDialogFragment.TAG);
					activity.getSupportFragmentManager().executePendingTransactions(); // show dialog before setting progress
				}

				pd.setProgress(intent.getIntExtra(SearchGeocacheService.PARAM_CURRENT, 0));
				break;

			case SearchGeocacheService.ACTION_PROGRESS_COMPLETE:
				if (pd != null && pd.isShowing())
					pd.dismiss();

				if (intent.getIntExtra(SearchGeocacheService.PARAM_COUNT, 0) != 0 && !activity.isFinishing()) {
					activity.finish();
				}
				break;

			case ErrorActivity.ACTION_ERROR:
				if (pd != null && pd.isShowing())
					pd.dismiss();

				Intent errorIntent = new Intent(intent);
				errorIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				activity.startActivity(errorIntent);
				break;
		}
	}

	@Override
	public void onCancel(AbstractDialogFragment dialogFragment) {
		SearchNearestActivity activity = activityRef.get();
		if (activity == null)
			return;

		if (pd != null && pd.isShowing())
			pd.dismiss();

		pd = null;
		activity.stopService(new Intent(activity, SearchGeocacheService.class));
	}
}
