package com.arcao.geocaching4locus.fragment;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.format.DateFormat;
import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.authentication.helper.AccountRestrictions;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.SpannedFix;

import java.lang.ref.WeakReference;

public class FullCacheDownloadConfirmDialogFragment extends AbstractDialogFragment {
	public static final String TAG = FullCacheDownloadConfirmDialogFragment.class.getName();

	public interface OnFullCacheDownloadConfirmDialogListener {
		void onFullCacheDownloadConfirmDialogFinished(boolean success);
	}

	protected WeakReference<OnFullCacheDownloadConfirmDialogListener> fullCacheDownloadConfirmDialogListenerRef;

	public static FullCacheDownloadConfirmDialogFragment newInstance() {
		return new FullCacheDownloadConfirmDialogFragment();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setCancelable(false);
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		try {
			fullCacheDownloadConfirmDialogListenerRef = new WeakReference<>((OnFullCacheDownloadConfirmDialogListener) activity);
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString() + " must implement OnFullCacheDownloadConfirmDialogListener");
		}
	}

	@NonNull
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AccountRestrictions restrictions = Geocaching4LocusApplication.getAuthenticatorHelper().getRestrictions();

		// apply format on a text
		int cachesPerPeriod = (int) restrictions.getMaxFullGeocacheLimit();
		int period = (int) restrictions.getFullGeocacheLimitPeriod();

		int cachesLeft = (int) restrictions.getFullGeocacheLimitLeft();

		String periodString;
		if (period < AppConstants.SECONDS_PER_MINUTE) {
			periodString = getResources().getQuantityString(R.plurals.plurals_minute, period, period);
		} else {
			period = period / AppConstants.SECONDS_PER_MINUTE;
			periodString = getResources().getQuantityString(R.plurals.plurals_hour, period, period);
		}

		String cacheString = getResources().getQuantityString(R.plurals.plurals_cache, cachesPerPeriod, cachesPerPeriod);
		String cachesLeftString = getResources().getQuantityString(R.plurals.plurals_cache, cachesLeft, cachesLeft);
		String renewTime = DateFormat.getTimeFormat(getActivity()).format(restrictions.getRenewFullGeocacheLimit());

		String message = getString(R.string.basic_member_full_geocache_warning_message, cacheString, periodString, cachesLeftString, renewTime);

		return new AlertDialog.Builder(getActivity())
			.setTitle(R.string.basic_member_warning_title)
			.setMessage(SpannedFix.fromHtml(message))
			.setPositiveButton(R.string.button_yes, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OnFullCacheDownloadConfirmDialogListener listener = fullCacheDownloadConfirmDialogListenerRef.get();
					if (listener != null) {
						listener.onFullCacheDownloadConfirmDialogFinished(true);
					}
				}
			})
			.setNegativeButton(R.string.button_no, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					OnFullCacheDownloadConfirmDialogListener listener = fullCacheDownloadConfirmDialogListenerRef.get();
					if (listener != null) {
						listener.onFullCacheDownloadConfirmDialogFinished(false);
					}
				}
			})
			.create();
	}
}
