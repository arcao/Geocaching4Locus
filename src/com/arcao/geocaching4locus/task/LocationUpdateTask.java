package com.arcao.geocaching4locus.task;

import java.lang.ref.WeakReference;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import com.arcao.geocaching4locus.Geocaching4LocusApplication;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.CustomDialogFragment.Cancellable;
import com.arcao.geocaching4locus.fragment.LocationUpdateProgressDialogFragment;
import com.arcao.geocaching4locus.util.UserTask;

public class LocationUpdateTask extends UserTask<Void, Void, Location> implements LocationListener, Cancellable<LocationUpdateProgressDialogFragment> {
	private static final String TAG = LocationUpdateTask.class.getName();
	private static final int TIMEOUT = 120; // in sec
	
	protected final CyclicBarrier barrier = new CyclicBarrier(2); // task + location update callback
	protected LocationUpdateProgressDialogFragment pd;
	
	protected WeakReference<FragmentActivity> activityRef;
	protected LocationManager locationManager;
	protected Location bestLocation;

  public LocationUpdateTask(FragmentActivity activity) {
    attach(activity);
    
    locationManager = (LocationManager) Geocaching4LocusApplication.getAppContext().getSystemService(Context.LOCATION_SERVICE);
  }

  public void attach(FragmentActivity activity) {
    activityRef = new WeakReference<FragmentActivity>(activity);
  }

  public void detach() {
  	cancel();
  }

	
	@Override
	protected void onPreExecute() {
		if (activityRef.get() == null) {
			cancel();
			return;
		}
		
		int source = LocationUpdateProgressDialogFragment.SOURCE_NETWORK;
		bestLocation = getLastLocation();
		
		FragmentActivity activity = activityRef.get();
		if (activity != null && activity instanceof LocationUpdate) {
			((LocationUpdate)activity).onLocationUpdate(bestLocation);
		}
		
		if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Log.i(TAG, "Searching location via " + LocationManager.GPS_PROVIDER);

			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			source = LocationUpdateProgressDialogFragment.SOURCE_GPS;
		} else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Log.i(TAG, "Searching location via " + LocationManager.NETWORK_PROVIDER);
			
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			Log.i(TAG, "No location providers found, used last location.");
			
			cancel();
			return;
		}
		
		pd = LocationUpdateProgressDialogFragment.newInstance(source, this);
		pd.show(activityRef.get().getSupportFragmentManager());
	}
	
	protected Location getLastLocation() {
		// use last available location			
		Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
		
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Geocaching4LocusApplication.getAppContext());
		
		Location location;
		if (gpsLocation == null && networkLocation != null) {
			location = networkLocation;
		} else if (gpsLocation != null && networkLocation == null) {
			location = gpsLocation;
		} else if (gpsLocation != null && networkLocation != null) {
			location = (networkLocation.getTime() < gpsLocation.getTime()) ? gpsLocation : networkLocation;
		} else {
			location = new Location(LocationManager.PASSIVE_PROVIDER);
			location.setLatitude(prefs.getFloat(PrefConstants.LAST_LATITUDE, 0));
			location.setLongitude(prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0));
		}
		
		Log.i(TAG, "Last location found for: " + location.toString());
		
		return location;
	}
	
	
	@Override
	protected Location doInBackground(Void... params) throws Exception {
		try {
			barrier.await(TIMEOUT, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			Log.i(TAG, "Barrier timeouted");
		} catch (BrokenBarrierException e) {
			Log.i(TAG, "Barrier cancelled");
		} finally {
			Log.i(TAG, "Location listener removed.");
			locationManager.removeUpdates(this);
		}
		return bestLocation;
	}
	
	@Override
	protected void onPostExecute(Location result) {
		FragmentActivity activity = activityRef.get();
		if (activity != null && activity instanceof LocationUpdate) {
			((LocationUpdate)activity).onLocationUpdate(bestLocation);
		}
	}
	
	@Override
	protected void onFinally() {
		if (pd != null)
			pd.dismiss();
	}
	
	public void cancel() {
		cancel(false);
		barrier.reset();
	}
		
	@Override
	public void onCancel(LocationUpdateProgressDialogFragment customDialogFragment) {
		cancel();
	}
	
	
	@Override
	public void onLocationChanged(Location location) {
		bestLocation = location;
		try {
			barrier.await(0, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Log.e(TAG, e.getMessage(), e);
		}
	}


	@Override
	public void onProviderDisabled(String provider) {
		Log.i(TAG, "Location provider " + provider + " disabled.");

		locationManager.removeUpdates(this);
		Log.i(TAG, "Location listener removed.");
		
		if (LocationManager.GPS_PROVIDER.equals(provider) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Log.i(TAG, "Switching to " + LocationManager.NETWORK_PROVIDER + " provider.");
			
			locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			cancel();
		}
	}


	@Override
	public void onProviderEnabled(String provider) {
	}


	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
	}
	
	
	
	
	
	public abstract static interface LocationUpdate {
		public abstract void onLocationUpdate(Location location);
	}
}
