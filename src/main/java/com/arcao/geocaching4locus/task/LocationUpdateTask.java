package com.arcao.geocaching4locus.task;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;
import com.arcao.geocaching4locus.R;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.fragment.dialog.AbstractDialogFragment;
import com.arcao.geocaching4locus.fragment.dialog.LocationUpdateProgressDialogFragment;
import com.arcao.geocaching4locus.util.UserTask;
import timber.log.Timber;

import java.lang.ref.WeakReference;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class LocationUpdateTask extends UserTask<Void, Void, Location> implements LocationListener, AbstractDialogFragment.CancellableDialog {
	private static final int TIMEOUT = 120; // in sec
	private static final String PASSIVE_PROVIDER = "passive";

	protected final CyclicBarrier mBarrier = new CyclicBarrier(2); // task + location update callback
	protected final WeakReference<FragmentActivity> mActivityRef;
	protected final Context mContext;
	protected final LocationManager mLocationManager;
	protected LocationUpdateProgressDialogFragment mDialog;
	protected Location mBestLocation;

	public LocationUpdateTask(FragmentActivity activity) {
		mActivityRef = new WeakReference<>(activity);
		mContext = activity.getApplicationContext();
		mLocationManager = (LocationManager) mContext.getSystemService(Context.LOCATION_SERVICE);
	}

	public void detach() {
		cancel();
	}

	@Override
	protected void onPreExecute() {
		FragmentActivity activity = mActivityRef.get();

		if (activity == null) {
			cancel();
			return;
		}

		int source = LocationUpdateProgressDialogFragment.SOURCE_NETWORK;
		mBestLocation = getLastLocation();

		if (activity instanceof LocationUpdate) {
			((LocationUpdate)activity).onLocationUpdate(mBestLocation);
		}

		if (mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
			Timber.i("Searching location via " + LocationManager.GPS_PROVIDER);

			mLocationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
			source = LocationUpdateProgressDialogFragment.SOURCE_GPS;
		} else if (mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Timber.i("Searching location via " + LocationManager.NETWORK_PROVIDER);

			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		} else {
			Timber.i("No location providers found, used last location.");

			cancel();

			NoLocationProviderDialogFragment.newInstance().show(activity.getSupportFragmentManager(), NoLocationProviderDialogFragment.TAG);
			return;
		}

		mDialog = LocationUpdateProgressDialogFragment.newInstance(source);
		mDialog.show(activity.getSupportFragmentManager(), LocationUpdateProgressDialogFragment.FRAGMENT_TAG);
	}

	protected Location getLastLocation() {
		// use last available location
		Location gpsLocation = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
		Location networkLocation = mLocationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(mContext);

		Location location;
		if (gpsLocation == null && networkLocation != null) {
			location = networkLocation;
		} else if (gpsLocation != null && networkLocation == null) {
			location = gpsLocation;
		} else if (gpsLocation != null) {
			location = (networkLocation.getTime() < gpsLocation.getTime()) ? gpsLocation : networkLocation;
		} else {
			location = new Location(PASSIVE_PROVIDER);
			location.setLatitude(prefs.getFloat(PrefConstants.LAST_LATITUDE, 0));
			location.setLongitude(prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0));
		}

		Timber.i("Last location found for: " + location.toString());

		return location;
	}


	@Override
	protected Location doInBackground(Void... params) throws Exception {
		try {
			mBarrier.await(TIMEOUT, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			Timber.i("Barrier timeouted");
		} catch (BrokenBarrierException e) {
			Timber.i("Barrier cancelled");
		} finally {
			Timber.i("Location listener removed.");
			mLocationManager.removeUpdates(this);
		}
		return mBestLocation;
	}

	@Override
	protected void onPostExecute(Location result) {
		Activity activity = mActivityRef.get();
		if (activity != null && activity instanceof LocationUpdate) {
			((LocationUpdate)activity).onLocationUpdate(mBestLocation);
		}
	}

	@Override
	protected void onFinally() {
		if (mDialog != null && mDialog.isShowing())
			mDialog.dismiss();
	}

	public void cancel() {
		cancel(false);
		mBarrier.reset();
	}

	@Override
	public void onCancel(AbstractDialogFragment dialogFragment) {
		cancel();
	}


	@Override
	public void onLocationChanged(Location location) {
		Timber.i("New location found for: " + location.toString());

		mBestLocation = location;
		try {
			mBarrier.await(0, TimeUnit.MILLISECONDS);
		} catch (Exception e) {
			Timber.e(e.getMessage(), e);
		}
	}


	@Override
	public void onProviderDisabled(String provider) {
		Timber.i("Location provider " + provider + " disabled.");

		mLocationManager.removeUpdates(this);
		Timber.i("Location listener removed.");

		if (LocationManager.GPS_PROVIDER.equals(provider) && mLocationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
			Timber.i("Switching to " + LocationManager.NETWORK_PROVIDER + " provider.");

			mLocationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
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

	public static class NoLocationProviderDialogFragment extends AbstractDialogFragment {
		public static final String TAG = NoLocationProviderDialogFragment.class.getName();

		public NoLocationProviderDialogFragment() {
			super();
		}

		public static AbstractDialogFragment newInstance() {
			return new NoLocationProviderDialogFragment();
		}

		@NonNull
		@Override
		public Dialog onCreateDialog(Bundle savedInstanceState) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setMessage(R.string.error_location);
			builder.setTitle(R.string.error_location_title);
			builder.setPositiveButton(R.string.ok_button, null);
			builder.setNeutralButton(R.string.error_location_settings_button, new OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					getActivity().startActivity(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS));
				}
			});
			return builder.create();
		}
	}
}
