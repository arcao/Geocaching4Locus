package com.arcao.geocaching4locus.receiver;

import static locus.api.android.utils.PeriodicUpdatesConst.VAR_LOC_MAP_BBOX_TOP_LEFT;
import static locus.api.android.utils.PeriodicUpdatesConst.VAR_LOC_MAP_CENTER;
import locus.api.android.PeriodicUpdate;
import locus.api.android.UpdateContainer;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.PeriodicUpdatesConst;
import locus.api.objects.extra.Location;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.service.LiveMapService;
import com.arcao.geocaching4locus.util.LocusTesting;

public class LiveMapBroadcastReceiver extends BroadcastReceiver {
	// Limitation on Groundpseak side to 100000 meters
	private static final float MAX_DIAGONAL_DISTANCE = 100000F;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent == null || intent.getAction() == null)
			return;

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (!prefs.getBoolean(PrefConstants.LIVE_MAP, false)) {
			return;
		}

		// Test if correct Locus version is installed
		if (!LocusTesting.isLocusInstalled(context)) {
			LocusTesting.showLocusTooOldToast(context);

			// disable live map
			prefs.edit().putBoolean(PrefConstants.LIVE_MAP, false).commit();
		}

		// ignore onTouch events
		if (intent.getBooleanExtra(PeriodicUpdatesConst.VAR_B_MAP_USER_TOUCHES, false))
			return;

		// temporary fix for NPE bug (locMapCenter can be null)
		if (LocusUtils.getLocationFromIntent(intent, VAR_LOC_MAP_CENTER) == null)
			return;

		// get valid instance of PeriodicUpdate object
		PeriodicUpdate pu = PeriodicUpdate.getInstance();

		// set notification of new locations
		pu.setLocNotificationLimit(computeNotificationLimit(intent));

		// handle event
		pu.onReceive(context, intent, new PeriodicUpdate.OnUpdate() {

			@Override
			public void onIncorrectData() {
			}

			@Override
			public void onUpdate(UpdateContainer update) {
				// sending data back to Locus based on events if has a new map center or zoom level and map is visible
				if (!update.isMapVisible())
					return;

				if (!update.isNewMapCenter() && !update.isNewZoomLevel())
					return;

				// When Live map is enabled, Locus sometimes send NaN when is starting
				if (Double.isNaN(update.getMapTopLeft().getLatitude()) || Double.isNaN(update.getMapTopLeft().getLongitude())
						|| Double.isNaN(update.getMapBottomRight().getLatitude()) || Double.isNaN(update.getMapBottomRight().getLongitude()))
					return;

				if (update.getMapTopLeft().distanceTo(update.getMapBottomRight()) >= MAX_DIAGONAL_DISTANCE)
					return;

				Location l = update.getLocMapCenter();

				// Start service to download caches
				context.startService(LiveMapService.createIntent(
						context,
						l.getLatitude(),
						l.getLongitude(),
						update.getMapTopLeft().getLatitude(),
						update.getMapTopLeft().getLongitude(),
						update.getMapBottomRight().getLatitude(),
						update.getMapBottomRight().getLongitude()
				));
			}
		});
	}

	protected static float computeNotificationLimit(Intent i) {
		Location locMapCenter = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_CENTER);
		Location mapTopLeft = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_BBOX_TOP_LEFT);

		if (locMapCenter == null || mapTopLeft == null)
			return 100F;

		return mapTopLeft.distanceTo(locMapCenter) / 2.5F;
	}
}
