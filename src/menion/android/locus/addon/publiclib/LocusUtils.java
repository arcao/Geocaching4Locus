package menion.android.locus.addon.publiclib;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;

public class LocusUtils {

	public static boolean isLocusAvailable(Context context) {
	    try {
	        // set intent
	        final PackageManager packageManager = context.getPackageManager();
	        final Intent intent = new Intent(Intent.ACTION_VIEW);
	        intent.setData(Uri.parse("menion.points:x"));
	         
	        // return true or false
	        return packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY).size() > 0;
	    } catch (Exception e) {
	        return false;
	    }
	}
}
