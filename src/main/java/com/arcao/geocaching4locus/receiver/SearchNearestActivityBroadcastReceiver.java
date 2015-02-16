package com.arcao.geocaching4locus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import com.arcao.geocaching4locus.ErrorActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.SearchNearestActivity;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.DownloadProgressDialogFragment;
import com.arcao.geocaching4locus.service.SearchGeocacheService;
import timber.log.Timber;

import java.lang.ref.WeakReference;

public class SearchNearestActivityBroadcastReceiver extends BroadcastReceiver implements AbstractDialogFragment.CancellableDialog {
	protected final WeakReference<SearchNearestActivity> mActivityRef;
	protected DownloadProgressDialogFragment mDialog;

	public SearchNearestActivityBroadcastReceiver(SearchNearestActivity activity) {
		mActivityRef = new WeakReference<>(activity);
	}

	public void register(Context ctx) {
		IntentFilter filter = new IntentFilter();
		filter.addAction(SearchGeocacheService.ACTION_PROGRESS_UPDATE);
		filter.addAction(SearchGeocacheService.ACTION_PROGRESS_COMPLETE);
		filter.addAction(ErrorActivity.ACTION_ERROR);

		LocalBroadcastManager.getInstance(ctx).registerReceiver(this, filter);
		Timber.i("Receiver registered.");
	}

	public void unregister(Context ctx) {
		LocalBroadcastManager.getInstance(ctx).unregisterReceiver(this);

		if (mDialog != null)
			mDialog.dismiss();

		Timber.i("Receiver unregistered.");
	}

	@Override
	public synchronized void onReceive(Context context, final Intent intent) {
		SearchNearestActivity activity = mActivityRef.get();
		if (activity == null)
			return;

		if (mDialog == null)
			mDialog = (DownloadProgressDialogFragment) activity.getFragmentManager().findFragmentByTag(DownloadProgressDialogFragment.FRAGMENT_TAG);

		switch(intent.getAction()) {
			case SearchGeocacheService.ACTION_PROGRESS_UPDATE:
				if (mDialog == null || !mDialog.isShowing()) {
					mDialog = DownloadProgressDialogFragment.newInstance(R.string.downloading, intent.getIntExtra(SearchGeocacheService.PARAM_COUNT, 1), intent.getIntExtra(SearchGeocacheService.PARAM_CURRENT, 0));
					mDialog.setOnCancelListener(this);
					mDialog.show(activity.getFragmentManager(), DownloadProgressDialogFragment.FRAGMENT_TAG);
					activity.getSupportFragmentManager().executePendingTransactions(); // show dialog before setting progress
				}

				mDialog.setProgress(intent.getIntExtra(SearchGeocacheService.PARAM_CURRENT, 0));
				break;

			case SearchGeocacheService.ACTION_PROGRESS_COMPLETE:
				if (mDialog != null && mDialog.isShowing())
					mDialog.dismiss();

				if (intent.getIntExtra(SearchGeocacheService.PARAM_COUNT, 0) != 0 && !activity.isFinishing()) {
					activity.finish();
				}
				break;

			case ErrorActivity.ACTION_ERROR:
				if (mDialog != null && mDialog.isShowing())
					mDialog.dismiss();

				Intent errorIntent = new Intent(intent);
				errorIntent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
				activity.startActivity(errorIntent);
				break;
		}
	}

	@Override
	public void onCancel(AbstractDialogFragment dialogFragment) {
		SearchNearestActivity activity = mActivityRef.get();
		if (activity == null)
			return;

		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();

		mDialog = null;
		activity.stopService(new Intent(activity, SearchGeocacheService.class));
	}
}
