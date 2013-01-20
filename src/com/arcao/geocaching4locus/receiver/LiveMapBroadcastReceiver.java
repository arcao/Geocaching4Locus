package com.arcao.geocaching4locus.receiver;

import static locus.api.android.utils.PeriodicUpdatesConst.VAR_LOC_MAP_BBOX_TOP_LEFT;
import static locus.api.android.utils.PeriodicUpdatesConst.VAR_LOC_MAP_CENTER;
import locus.api.android.PeriodicUpdate;
import locus.api.android.PeriodicUpdate.UpdateContainer;
import locus.api.android.utils.LocusUtils;
import locus.api.android.utils.PeriodicUpdatesConst;
import locus.api.objects.extra.Location;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.service.LiveMapService;

public class LiveMapBroadcastReceiver extends BroadcastReceiver {
	// Limitation on Groundpseak side to 100000 meters
	private static final float MAX_DIAGONAL_DISTANCE = 100000F;
	
	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent == null || intent.getAction() == null)
			return;
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		
		if (!prefs.getBoolean("live_map", false)) {
			return;
		}
		
		// ignore onTouch events
		if (intent.getBooleanExtra(PeriodicUpdatesConst.VAR_B_MAP_USER_TOUCHES, false))
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
				if (!update.mapVisible)
					return;
				
				if (!update.newMapCenter && !update.newZoomLevel)
					return;
				
				// When Live map is enabled, Locus sometimes send NaN when is starting
				if (Double.isNaN(update.mapTopLeft.getLatitude()) || Double.isNaN(update.mapTopLeft.getLongitude())
						|| Double.isNaN(update.mapBottomRight.getLatitude()) || Double.isNaN(update.mapBottomRight.getLongitude()))
					return;
				
				if (update.mapTopLeft.distanceTo(update.mapBottomRight) >= MAX_DIAGONAL_DISTANCE)
					return;
								
				Location l = update.locMapCenter;
				
				// Start service to download caches
				context.startService(LiveMapService.createIntent(
						context,
						l.getLatitude(),
						l.getLongitude(),
						update.mapTopLeft.getLatitude(),
						update.mapTopLeft.getLongitude(),
						update.mapBottomRight.getLatitude(),
						update.mapBottomRight.getLongitude()
				));
			}
		});
	}
	
	protected static float computeNotificationLimit(Intent i) {
		Location locMapCenter = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_CENTER);
		Location mapTopLeft = LocusUtils.getLocationFromIntent(i, VAR_LOC_MAP_BBOX_TOP_LEFT);
		
		return mapTopLeft.distanceTo(locMapCenter) / 2.5F;
	}
}
