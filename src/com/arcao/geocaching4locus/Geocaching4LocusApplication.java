package com.arcao.geocaching4locus;

import java.util.UUID;

import org.acra.ACRA;
import org.acra.ErrorReporter;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;

import android.Manifest.permission;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.preference.PreferenceManager;
import android.util.Log;

import com.arcao.geocaching4locus.authentication.helper.AccountAuthenticatorHelper;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.authentication.helper.PreferenceAuthenticatorHelper;
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
  private static final String TAG = "G4L|Geocaching4LocusApplication";
  
	private static Context context;
	private static AuthenticatorHelper authenticatorHelper;
	private static String deviceId;

	@Override
	public void onCreate() {
		context = getApplicationContext();
		
		// The following line triggers the initialization of ACRA
		ACRA.init(this);
		
    PackageManager pm = getPackageManager();
    if (pm != null && pm.checkPermission(permission.MANAGE_ACCOUNTS, getPackageName()) == PackageManager.PERMISSION_GRANTED) {
    	authenticatorHelper = new AccountAuthenticatorHelper(this);
    } else {
    	authenticatorHelper = new PreferenceAuthenticatorHelper(this);
    }

		authenticatorHelper.convertFromOldStorage();
		
		if (authenticatorHelper.hasAccount()) {
			ErrorReporter.getInstance().putCustomData("userName", authenticatorHelper.getAccount().name);
		}
		
		System.setProperty("debug", "1");
		
		super.onCreate();
	}
	
	public static Context getAppContext() {
		return context;
	}
	
	public static AuthenticatorHelper getAuthenticatorHelper() {
		return authenticatorHelper;
	}
	
	public static String getDeviceId() {
    SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(context);
    
    if (deviceId == null) {
      deviceId = pref.getString("device_id", null);
    }
    
    if (deviceId == null) {   
      deviceId = UUID.randomUUID().toString();
      pref.edit().putString("device_id", deviceId).commit();
    }
    
    return deviceId;
  }

	public static String getVersion() {
	  try {
	    return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
	  } catch (NameNotFoundException e) {
	    Log.e(TAG, e.getMessage(), e);
	    return "1.0";
	  }
	}
}
