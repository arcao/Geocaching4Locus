package com.arcao.geocaching4locus;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.arcao.geocaching4locus.authentication.AccountAuthenticator;
import com.arcao.geocaching4locus.constants.AppConstants;

@ReportsCrashes(
		formKey = AppConstants.ERROR_FORM_KEY)
public class Geocaching4LocusApplication extends Application {

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		AccountAuthenticator.convertFromOldStorage(this);
		super.onCreate();
	}
}
