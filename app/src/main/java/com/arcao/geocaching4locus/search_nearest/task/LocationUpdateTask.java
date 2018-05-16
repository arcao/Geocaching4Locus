package com.arcao.geocaching4locus.search_nearest.task;

import android.Manifest;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.arcao.geocaching4locus.base.constants.PrefConstants;
import com.arcao.geocaching4locus.base.task.UserTask;

import java.lang.ref.WeakReference;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import timber.log.Timber;

public class LocationUpdateTask extends UserTask<Void, Void, Location> implements LocationListener {
    private static final int TIMEOUT = 120; // in sec

    public interface TaskListener {
        void onTaskFinished(Location location);

        void onProviderChanged(String provider);
    }

    private final Context context;
    private final WeakReference<TaskListener> taskListenerRef;
    private final CyclicBarrier barrier = new CyclicBarrier(2); // task + location update callback
    private final LocationManager locationManager;
    private Location bestLocation;

    public LocationUpdateTask(Context context, TaskListener listener) {
        taskListenerRef = new WeakReference<>(listener);
        this.context = context.getApplicationContext();
        locationManager = (LocationManager) this.context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    protected void onPreExecute() {
        bestLocation = getLastLocation();

        String provider = null;

        if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
                checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            provider = LocationManager.GPS_PROVIDER;
        } else if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) &&
                checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            provider = LocationManager.NETWORK_PROVIDER;
        }

        if (provider == null) {
            Timber.i("No location providers found, used last location.");

            fireOnTaskFinished(null);
            cancel(true);
            return;
        }

        Timber.i("Searching location via %s", provider);
        fireOnProviderChanged(provider);
        locationManager.requestLocationUpdates(provider, 0, 0, this);
    }

    private Location getLastLocation() {
        // use last available location

        Location gpsLocation = null;
        Location networkLocation = null;

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);

        if (checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED)
            networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        Location location;
        if (gpsLocation == null && networkLocation != null) {
            location = networkLocation;
        } else if (gpsLocation != null && networkLocation == null) {
            location = gpsLocation;
        } else if (gpsLocation != null) {
            location = (networkLocation.getTime() < gpsLocation.getTime()) ? gpsLocation : networkLocation;
        } else {
            location = new Location(LocationManager.PASSIVE_PROVIDER);
            location.setLatitude(prefs.getFloat(PrefConstants.LAST_LATITUDE, 0));
            location.setLongitude(prefs.getFloat(PrefConstants.LAST_LONGITUDE, 0));
        }

        Timber.i("Last location found for: %s", location);

        return location;
    }


    @Override
    protected Location doInBackground(Void... params) throws Exception {
        try {
            barrier.await(TIMEOUT, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            Timber.i("Barrier timeout");
        } catch (BrokenBarrierException e) {
            Timber.i("Barrier cancelled");
        } finally {

            if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                    checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.removeUpdates(this);
            }
            Timber.i("Location listener removed.");
        }
        return bestLocation;
    }

    @Override
    protected void onPostExecute(Location result) {
        fireOnTaskFinished(result);
    }

    @Override
    protected void onCancelled() {
        barrier.reset();
        super.onCancelled();
    }

    private int checkSelfPermission(@NonNull String permission) {
        return ContextCompat.checkSelfPermission(context, permission);
    }

    private void fireOnProviderChanged(String provider) {
        Timber.i("fireOnProviderChanged: %s", provider);
        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onProviderChanged(provider);
    }

    private void fireOnTaskFinished(Location location) {
        Timber.i("fireOnTaskFinished: %s", location);
        TaskListener listener = taskListenerRef.get();
        if (listener != null)
            listener.onTaskFinished(location);
    }

    // --------------------- LocationListener methods ---------------------
    @Override
    public void onLocationChanged(Location location) {
        Timber.i("onLocationChanged: %s", location);

        bestLocation = location;
        try {
            barrier.await(0, TimeUnit.MILLISECONDS);
        } catch (Exception e) {
            // log nothing
        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        Timber.i("onProviderDisabled: %s", provider);

        // No permission? Cancel task
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            cancel(true);
            return;
        }

        locationManager.removeUpdates(this);
        Timber.i("Location listener removed.");

        if (LocationManager.GPS_PROVIDER.equals(provider) && locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Timber.i("Switching to " + LocationManager.NETWORK_PROVIDER + " provider.");

            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
        } else {
            fireOnTaskFinished(null);
            cancel(true);
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
    }
}
