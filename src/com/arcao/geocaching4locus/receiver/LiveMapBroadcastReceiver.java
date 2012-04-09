package com.arcao.geocaching4locus.receiver;

import menion.android.locus.addon.publiclib.PeriodicUpdate;
import menion.android.locus.addon.publiclib.PeriodicUpdate.UpdateContainer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.preference.PreferenceManager;

import com.arcao.geocaching4locus.service.LiveMapService;

public class LiveMapBroadcastReceiver extends BroadcastReceiver {
	@Override
	public void onReceive(final Context context, final Intent intent) {
		if (intent == null || intent.getAction() == null)
			return;
		
		final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

		if (!prefs.getBoolean("live_map", false)) {
			return;
		}
				
		// get valid instance of PeriodicUpdate object
		PeriodicUpdate pu = PeriodicUpdate.getInstance();

		// set notification of new locations to 100m
		pu.setLocNotificationLimit(100);
		
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
								
				Location l = PeriodicUpdate.getInstance().getLastMapCenter();
				
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
}
