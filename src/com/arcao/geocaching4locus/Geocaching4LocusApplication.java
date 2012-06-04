package com.arcao.geocaching4locus;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;

import com.arcao.geocaching4locus.authentication.AccountAuthenticator;
import com.arcao.geocaching4locus.constants.AppConstants;

@ReportsCrashes(
		formKey = AppConstants.ERROR_FORM_KEY,
		mode = ReportingInteractionMode.NOTIFICATION,
    resNotifTickerText = R.string.crash_notif_ticker_text,
    resNotifTitle = R.string.crash_notif_title,
    resNotifText = R.string.crash_notif_text,
    resNotifIcon = android.R.drawable.stat_notify_error, // optional. default is a warning sign
    resDialogText = R.string.crash_dialog_text,
    resDialogIcon = android.R.drawable.ic_dialog_info, //optional. default is a warning sign
    resDialogTitle = R.string.crash_dialog_title, // optional. default is your application name
    resDialogCommentPrompt = R.string.crash_dialog_comment_prompt, // optional. when defined, adds a user text field input with this text resource as a label
    resDialogOkToast = R.string.crash_dialog_ok_toast // optional. displays a Toast message when the user accepts to send a report.
)
public class Geocaching4LocusApplication extends Application {

	@Override
	public void onCreate() {
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		AccountAuthenticator.convertFromOldStorage(this);
		
		if (AccountAuthenticator.hasAccount(this)) {
			ErrorReporter.getInstance().putCustomData("userName", AccountAuthenticator.getAccount(this).name);
		}
		
		super.onCreate();
	}
}
