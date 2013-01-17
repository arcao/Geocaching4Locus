package com.arcao.geocaching4locus;

import org.acra.ErrorReporter;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.fragment.GCNumberInputDialogFragment;
import com.arcao.geocaching4locus.fragment.GCNumberInputDialogFragment.OnInputFinishedListener;
import com.arcao.geocaching4locus.fragment.ImportDialogFragment;
import com.arcao.geocaching4locus.task.ImportTask.OnTaskFinishedListener;
import com.arcao.geocaching4locus.util.LocusTesting;

public class ImportFromGCActivity extends FragmentActivity implements OnTaskFinishedListener, OnInputFinishedListener {
	private static final String TAG = ImportFromGCActivity.class.getName();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		if (!LocusTesting.isLocusInstalled(this)) {
			LocusTesting.showLocusMissingError(this);
			return;
		}
		
		// if import task is running, show the import task progress dialog
		ImportDialogFragment fragment = (ImportDialogFragment) getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG);
		if (fragment != null) {
			fragment.show(getSupportFragmentManager(), ImportDialogFragment.TAG);
			return;
		}
		
		showGCNumberInputDialog();
	}
	
	protected void showGCNumberInputDialog() {
		GCNumberInputDialogFragment fragment = (GCNumberInputDialogFragment) getSupportFragmentManager().findFragmentByTag(GCNumberInputDialogFragment.TAG);
		if (fragment == null) {
			fragment = GCNumberInputDialogFragment.newInstance();
		}
		
		fragment.show(getSupportFragmentManager(), GCNumberInputDialogFragment.TAG);
	}
	
	protected void startImport(String cacheId) {
		ErrorReporter.getInstance().putCustomData("source", "importFromGC;" + cacheId);
		
		ImportDialogFragment fragment = (ImportDialogFragment) getSupportFragmentManager().findFragmentByTag(ImportDialogFragment.TAG);
		if (fragment == null)
			fragment = ImportDialogFragment.newInstance(cacheId);
			
		fragment.show(getSupportFragmentManager(), ImportDialogFragment.TAG);
	}
	
	@Override
	public void onInputFinished(String input) {
		if (input != null) {
			startImport(input);
		} else {
			onTaskFinished(false);
		}
	}
	
	@Override
	public void onTaskFinished(boolean success) {
		Log.d(TAG, "onTaskFinished result: " + success);
		setResult(success ? RESULT_OK : RESULT_CANCELED);
		finish();
	}

}
