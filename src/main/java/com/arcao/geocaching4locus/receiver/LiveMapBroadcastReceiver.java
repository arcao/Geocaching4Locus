package com.arcao.geocaching4locus.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.v4.content.WakefulBroadcastReceiver;

import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.service.LiveMapService;
import com.arcao.geocaching4locus.util.LiveMapNotificationManager;
import com.arcao.geocaching4locus.util.LocusTesting;
import locus.api.android.features.periodicUpdates.PeriodicUpdatesHandler;
import locus.api.android.features.periodicUpdates.UpdateContainer;
import locus.api.android.utils.LocusUtils;
import locus.api.objects.extra.Location;

public class LiveMapBroadcastReceiver extends WakefulBroadcastReceiver {
	private static final String VAR_B_MAP_USER_TOUCHES = ("1306");
	private static final String VAR_LOC_MAP_CENTER = ("1302");
	private static final String VAR_LOC_MAP_BBOX_TOP_LEFT = ("1303");

	// Limitation on Groundspeak side to 100000 meters
	private static final float MAX_DIAGONAL_DISTANCE = 100000F;
	private static final float DEFAULT_DISTANCE_LIMIT = 100F;
	private static final float DISTANCE_LIMIT_DIVIDER = 2.5F;

	private static boolean mForceUpdate = false;

	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent == null || intent.getAction() == null)
			return;

		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		final LiveMapNotificationManager liveMapNotificationManager = LiveMapNotificationManager.get(context);

		if (liveMapNotificationManager.handleBroadcastIntent(intent)) {
			//noinspection AssignmentToStaticFieldFromInstanceMethod
			mForceUpdate = liveMapNotificationManager.isForceUpdateRequiredInFuture();
			return;
		}

		if (!prefs.getBoolean(PrefConstants.LIVE_MAP, false)) {
			return;
		}

		// Test if correct Locus version is installed
		if (!LocusTesting.isLocusInstalled(context)) {
			LocusTesting.showLocusTooOldToast(context);

			// disable live map
			liveMapNotificationManager.setLiveMapEnabled(false);
		}

		// ignore onTouch events
		if (intent.getBooleanExtra(VAR_B_MAP_USER_TOUCHES, false))
			return;

		// temporary fix for NPE bug (locMapCenter can be null)
		if (LocusUtils.getLocationFromIntent(intent, VAR_LOC_MAP_CENTER) == null)
			return;

		// get valid instance of PeriodicUpdate object
		PeriodicUpdatesHandler pu = PeriodicUpdatesHandler.getInstance();

		// set notification of new locations
		pu.setLocNotificationLimit(computeNotificationLimit(intent));

		// handle event
		pu.onReceive(context, intent, new PeriodicUpdatesHandler.OnUpdate() {

			@Override
			public void onIncorrectData() {
			}

			@Override
			public void onUpdate(LocusUtils.LocusVersion locusVersion, UpdateContainer update) {
				// sending data back to Locus based on events if has a new map center or zoom level and map is visible
				if (!update.isMapVisible())
					return;

				if (!update.isNewMapCenter() && !update.isNewZoomLevel() && !mForceUpdate)
					return;

				//noinspection AssignmentToStaticFieldFromInstanceMethod
				mForceUpdate = false;

				// When Live map is enabled, Locus sometimes send NaN when is starting
				if (Double.isNaN(update.getMapTopLeft().getLatitude()) || Double.isNaN(update.getMapTopLeft().getLongitude())
						|| Double.isNaN(update.getMapBottomRight().getLatitude()) || Double.isNaN(update.getMapBottomRight().getLongitude()))
					return;

				if (update.getMapTopLeft().distanceTo(update.getMapBottomRight()) >= MAX_DIAGONAL_DISTANCE)
					return; // Zoom is too low

				Location l = update.getLocMapCenter();

				// Start service to download caches
				startWakefulService(context, LiveMapService.createIntent(
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

	private static float computeNotificationLimit(Intent i) {
		Location locMapCenter = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_CENTER);
		Location mapTopLeft = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_BBOX_TOP_LEFT);

		if (locMapCenter == null || mapTopLeft == null)
			return DEFAULT_DISTANCE_LIMIT;

		return mapTopLeft.distanceTo(locMapCenter) / DISTANCE_LIMIT_DIVIDER;
	}
}
