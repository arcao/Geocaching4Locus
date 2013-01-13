package com.arcao.geocaching4locus.fragment;

import locus.api.android.utils.LocusUtils;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;

import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.util.LocusTesting;

public final class LocusTestingErrorDialogFragment extends AbstractErrorDialogFragment {
	public static final String TAG = LocusTestingErrorDialogFragment.class.getName();
	
	public static LocusTestingErrorDialogFragment newInstance() {
		Context context = Geocaching4LocusApplication.getAppContext();
		
		LocusTestingErrorDialogFragment fragment = new LocusTestingErrorDialogFragment();
		fragment.prepareDialog(R.string.error_title, LocusUtils.isLocusAvailable(context) ? R.string.error_locus_old : R.string.error_locus_not_found, AppConstants.LOCUS_MIN_VERSION.toString());

		return fragment;
	}
	
	
	@Override
	public OnClickListener getPositiveButtonOnClickListener() {
		return new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				FragmentActivity activity = getActivity();
				
				Uri localUri;
				if (LocusTesting.isAndroidMarketInstalled(activity)) {
					// create Uri for Locus Free on Google Play
					localUri = Uri.parse(AppConstants.GOOGLE_PLAY_PREFIX + LocusUtils.LOCUS_PACKAGE_NAMES[1]);
				} else {
					if (LocusUtils.isLocusProAvailable(activity, LocusUtils.LOCUS_API_SINCE_VERSION)) {
						localUri = AppConstants.ANDROIDPIT_LOCUS_PRO_LINK;
					} else {
						localUri = AppConstants.ANDROIDPIT_LOCUS_FREE_LINK;
					}
				}
				Intent localIntent = new Intent(Intent.ACTION_VIEW, localUri);
				activity.startActivity(localIntent);
				activity.finish();
			}
		};
	}
}
