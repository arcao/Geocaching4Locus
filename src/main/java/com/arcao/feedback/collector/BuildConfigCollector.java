package com.arcao.feedback.collector;

import android.content.Context;
import com.arcao.geocaching4locus.BuildConfig;
import hu.supercluster.paperwork.Paperwork;

public class BuildConfigCollector extends Collector {
	private final Paperwork mPaperwork;

	public BuildConfigCollector(Context context) {
		mPaperwork = new Paperwork(context);
	}

	@Override
	public String getName() {
		return "BuildConfig INFO";
	}

	@Override
	protected String collect() {

		return "APPLICATION_ID=" + BuildConfig.APPLICATION_ID +
						"\nBUILD_TIME=" + mPaperwork.get("buildTime") +
						"\nBUILD_TYPE=" + BuildConfig.BUILD_TYPE +
						"\nBUILD_BY=" + mPaperwork.get("buildBy") +
						"\nDEBUG=" + BuildConfig.DEBUG +
						"\nFLAVOR=" + BuildConfig.FLAVOR +
						"\nGEOCACHING_API_STAGING=" + BuildConfig.GEOCACHING_API_STAGING +
						"\nGIT_SHA=" + mPaperwork.get("gitSha") +
						"\nVERSION_CODE=" + BuildConfig.VERSION_CODE +
						"\nVERSION_NAME=" + BuildConfig.VERSION_NAME +
						"\n";
	}
}
