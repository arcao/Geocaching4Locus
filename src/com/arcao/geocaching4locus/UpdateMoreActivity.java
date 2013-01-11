package com.arcao.geocaching4locus;

import locus.api.android.utils.LocusUtils;

import org.acra.ErrorReporter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.fragment.UpdateMoreDialogFragment;
import com.arcao.geocaching4locus.task.UpdateMoreTask.OnTaskFinishedListener;

public class UpdateMoreActivity extends FragmentActivity implements OnTaskFinishedListener {
	private final static String TAG = "G4L|UpdateActivity";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		long[] pointIndexes = null; 
		
		if (LocusUtils.isIntentPointsScreenTools(getIntent())) {
      pointIndexes = LocusUtils.handleIntentPointsScreenTools(getIntent());      
		}

		if (pointIndexes.length == 0) {
			Log.e(TAG, "No caches received");
			setResult(RESULT_CANCELED);
			finish();
			return;
		}

		ErrorReporter.getInstance().putCustomData("source", "update;");
		ErrorReporter.getInstance().putCustomData("count", String.valueOf(pointIndexes.length));

		UpdateMoreDialogFragment fragment = (UpdateMoreDialogFragment) getSupportFragmentManager().findFragmentByTag(UpdateMoreDialogFragment.TAG);
		if (fragment == null)
			fragment = UpdateMoreDialogFragment.newInstance(pointIndexes);
			
		fragment.show(getSupportFragmentManager(), UpdateMoreDialogFragment.TAG);
	}
		
	@Override
	public void onTaskFinished(boolean success) {
		Log.d(TAG, "onTaskFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}
	
	@Override
	public void onProgressUpdate(int count) {
		// unused
	}
}
