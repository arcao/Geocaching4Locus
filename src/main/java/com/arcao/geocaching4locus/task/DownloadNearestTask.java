package com.arcao.geocaching4locus.task;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.arcao.geocaching.api.GeocachingApi;
import com.arcao.geocaching.api.GeocachingApiFactory;
import com.arcao.geocaching.api.data.Geocache;
import com.arcao.geocaching.api.data.coordinates.Coordinates;
import com.arcao.geocaching.api.data.type.ContainerType;
import com.arcao.geocaching.api.data.type.GeocacheType;
import com.arcao.geocaching.api.exception.GeocachingApiException;
import com.arcao.geocaching.api.exception.InvalidCredentialsException;
import com.arcao.geocaching.api.exception.InvalidSessionException;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.BookmarksExcludeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.DifficultyFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.Filter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheContainerSizeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheExclusionsFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.GeocacheTypeFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotFoundByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.NotHiddenByUsersFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.PointRadiusFilter;
import com.arcao.geocaching.api.impl.live_geocaching_api.filter.TerrainFilter;
import com.arcao.geocaching4locus.App;
import com.arcao.geocaching4locus.UpdateActivity;
import com.arcao.geocaching4locus.authentication.helper.AuthenticatorHelper;
import com.arcao.geocaching4locus.constants.AppConstants;
import com.arcao.geocaching4locus.constants.PrefConstants;
import com.arcao.geocaching4locus.exception.ExceptionHandler;
import com.arcao.geocaching4locus.exception.IntendedException;
import com.arcao.geocaching4locus.exception.LocusMapRuntimeException;
import com.arcao.geocaching4locus.exception.NoResultFoundException;
import com.arcao.geocaching4locus.util.ParcelFile;
import com.arcao.geocaching4locus.util.PreferenceUtil;
import com.arcao.geocaching4locus.util.UserTask;
import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import locus.api.android.ActionDisplayPointsExtended;
import locus.api.android.objects.PackWaypoints;
import locus.api.mapper.LocusDataMapper;
import locus.api.objects.extra.Waypoint;
import locus.api.utils.StoreableListFileOutput;
import locus.api.utils.Utils;
import timber.log.Timber;

public class DownloadNearestTask extends UserTask<Void, Integer, Intent> {
  private static final String PACK_WAYPOINTS_NAME = DownloadNearestTask.class.getName();

  private final Context mContext;
  private final SharedPreferences mPrefs;
  private final WeakReference<TaskListener> mTaskListenerRef;
  private final Coordinates mCoordinates;
  private final int mCount;
  private final double mDistance;

  public interface TaskListener {
    void onTaskFinished(Intent intent);
    void onTaskError(@NonNull Intent errorIntent);
    void onProgressUpdate(int current, int count);
  }

  public DownloadNearestTask(Context context, TaskListener listener, double latitude, double longitude, int count) {
    mContext = context.getApplicationContext();
    mTaskListenerRef = new WeakReference<>(listener);
    mCoordinates = new Coordinates(latitude, longitude);
    mCount = count;

    mPrefs = PreferenceManager.getDefaultSharedPreferences(mContext);
    mDistance = getDistance();
  }

  @Override
  protected void onPostExecute(Intent result) {
    super.onPostExecute(result);

    TaskListener listener = mTaskListenerRef.get();
    if (listener != null)
      listener.onTaskFinished(result);
  }

  @Override
  protected void onProgressUpdate(Integer... values) {
    TaskListener listener = mTaskListenerRef.get();
    if (listener != null)
      listener.onProgressUpdate(values[0], mCount);
  }


  @Override
  protected Intent doInBackground(Void... params) throws Exception {
    AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();
    if (!authenticatorHelper.hasAccount())
      throw new InvalidCredentialsException("Account not found.");

    if (isCancelled())
      return null;

    Timber.i("source=search;coordinates=" + mCoordinates + ";count=" + mCount);

    GeocachingApi api = GeocachingApiFactory.create();


    GeocachingApi.ResultQuality resultQuality = authenticatorHelper.getRestrictions().isPremiumMember() ?
        GeocachingApi.ResultQuality.FULL : GeocachingApi.ResultQuality.SUMMARY;

    int logCount = mPrefs.getInt(PrefConstants.DOWNLOADING_COUNT_OF_LOGS, 5);
    boolean simpleCacheData = mPrefs.getBoolean(PrefConstants.DOWNLOADING_SIMPLE_CACHE_DATA, false);
    if (simpleCacheData) {
      resultQuality = GeocachingApi.ResultQuality.LITE;
      logCount = 0;
    }

    int current = 0;
    ParcelFile dataFile = new ParcelFile(ActionDisplayPointsExtended.getCacheFileName(mContext));
    StoreableListFileOutput fileOutput = null;

    try {
      login(api);

      fileOutput = new StoreableListFileOutput(ActionDisplayPointsExtended.getCacheFileOutputStream(mContext));
      fileOutput.beginList();

      int cachesPerRequest = AppConstants.CACHES_PER_REQUEST;

      publishProgress(current);

      while (current < mCount) {
        long startTime = System.currentTimeMillis();

        List<Geocache> cachesToAdd;

        if (current == 0) {
          cachesToAdd = api.searchForGeocaches(resultQuality, Math.min(cachesPerRequest, mCount - current), logCount, 0, createFilters(), null);
        } else {
          cachesToAdd = api.getMoreGeocaches(resultQuality, current, Math.min(cachesPerRequest, mCount - current), logCount, 0);
        }

        authenticatorHelper.getRestrictions().updateLimits(api.getLastGeocacheLimits());

        if (isCancelled())
          return null;

        if (cachesToAdd.size() == 0)
          break;

        // FIX for not working distance filter
        if (computeDistance(mCoordinates, cachesToAdd.get(cachesToAdd.size() - 1)) > mDistance) {
          removeCachesOverDistance(cachesToAdd, mCoordinates, mDistance);

          if (cachesToAdd.size() == 0)
            break;
        }

        PackWaypoints pw = new PackWaypoints(PACK_WAYPOINTS_NAME);
        List<Waypoint> waypoints = LocusDataMapper.toLocusPoints(mContext, cachesToAdd);

        for (Waypoint wpt : waypoints) {
          if (simpleCacheData) {
            wpt.setExtraOnDisplay(mContext.getPackageName(), UpdateActivity.class.getName(), UpdateActivity.PARAM_SIMPLE_CACHE_ID, wpt.gcData.getCacheID());
          }

          pw.addWaypoint(wpt);
        }

        fileOutput.write(pw);

        current += cachesToAdd.size();

        publishProgress(current);
        long requestDuration = System.currentTimeMillis() - startTime;
        cachesPerRequest = computeCachesPerRequest(cachesPerRequest, requestDuration);
      }

      fileOutput.endList();

      Timber.i("found caches: " + current);
    } catch (InvalidSessionException e) {
      authenticatorHelper.invalidateAuthToken();

      throw handleException(e, fileOutput, dataFile);
    } catch (IOException e) {
      throw handleException(new GeocachingApiException(e.getMessage(), e), fileOutput, dataFile);
    } catch (Exception e) {
      throw handleException(e, fileOutput, dataFile);
    } finally {
      Utils.closeStream(fileOutput);
    }

    if (current > 0) {
      try {
        return ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true);
      } catch (Throwable t) {
        throw new LocusMapRuntimeException(t);
      }
    } else {
      throw new NoResultFoundException();
    }
  }

  private Exception handleException(@NonNull Exception e, @Nullable StoreableListFileOutput fileOutput, @Nullable File dataFile) {
    if (fileOutput == null || dataFile == null || fileOutput.getItemCount() == 0)
      return e;

    return new IntendedException(e, ActionDisplayPointsExtended.createSendPacksIntent(dataFile, true, true));
  }

  @Override
  protected void onCancelled() {
    super.onCancelled();

    TaskListener listener = mTaskListenerRef.get();
    if (listener != null)
      listener.onTaskFinished(null);
  }

  @Override
  protected void onException(Throwable t) {
    super.onException(t);

    if (isCancelled())
      return;

    Timber.e(t, t.getMessage());

    Intent intent = new ExceptionHandler(mContext).handle(t);
    //intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS | Intent.FLAG_ACTIVITY_NEW_TASK);

    TaskListener listener = mTaskListenerRef.get();
    if (listener != null)
      listener.onTaskError(intent);

    //mContext.startActivity(intent);
  }


  private List<Filter> createFilters() {
    ArrayList<Filter> filters = new ArrayList<>(9);

    AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();
    String userName = authenticatorHelper.getAccount().name;
    boolean premiumMember = authenticatorHelper.getRestrictions().isPremiumMember();

    filters.add(new PointRadiusFilter(mCoordinates.getLatitude(), mCoordinates.getLongitude(), (long) (mDistance * 1000)));

    boolean showDisabled = mPrefs.getBoolean(PrefConstants.FILTER_SHOW_DISABLED, false);
    filters.add(new GeocacheExclusionsFilter(false, showDisabled ? null : true, null));

    boolean showFound = mPrefs.getBoolean(PrefConstants.FILTER_SHOW_FOUND, false);
    if (showFound) {
      filters.add(new NotFoundByUsersFilter(userName));
    }

    boolean showOwn = mPrefs.getBoolean(PrefConstants.FILTER_SHOW_OWN, false);
    if (showOwn) {
      filters.add(new NotHiddenByUsersFilter(userName));
    }

    if (premiumMember) {
      filters.add(new GeocacheTypeFilter(getSelectedGeocacheTypes()));
      filters.add(new GeocacheContainerSizeFilter(getSelectedContainerTypes()));

      float difficultyMin = PreferenceUtil.getParsedFloat(mPrefs, PrefConstants.FILTER_DIFFICULTY_MIN, 1);
      float difficultyMax = PreferenceUtil.getParsedFloat(mPrefs, PrefConstants.FILTER_DIFFICULTY_MAX, 5);
      if (difficultyMin > 1 || difficultyMax < 5) {
        filters.add(new DifficultyFilter(difficultyMin, difficultyMax));
      }

      float terrainMin = PreferenceUtil.getParsedFloat(mPrefs, PrefConstants.FILTER_TERRAIN_MIN, 1);
      float terrainMax = PreferenceUtil.getParsedFloat(mPrefs, PrefConstants.FILTER_TERRAIN_MAX, 5);
      if (terrainMin > 1 || terrainMax < 5) {
        filters.add(new TerrainFilter(terrainMin, terrainMax));
      }

      // TODO: 3. 9. 2015 Move it to configuration
      boolean excludeIgnoreList = true;
      if (excludeIgnoreList) {
        filters.add(new BookmarksExcludeFilter(true));
      }
    }

    return filters;
  }

  private GeocacheType[] getSelectedGeocacheTypes() {
    Vector<GeocacheType> filter = new Vector<>(GeocacheType.values().length);

    for (int i = 0; i < GeocacheType.values().length; i++) {
      if (mPrefs.getBoolean(PrefConstants.FILTER_CACHE_TYPE_PREFIX + i, true)) {
        filter.add(GeocacheType.values()[i]);
      }
    }

    return filter.toArray(new GeocacheType[filter.size()]);
  }

  private ContainerType[] getSelectedContainerTypes() {
    Vector<ContainerType> filter = new Vector<>(ContainerType.values().length);

    for (int i = 0; i < ContainerType.values().length; i++) {
      if (mPrefs.getBoolean(PrefConstants.FILTER_CONTAINER_TYPE_PREFIX + i, true)) {
        filter.add(ContainerType.values()[i]);
      }
    }

    return filter.toArray(new ContainerType[filter.size()]);
  }


  private void login(GeocachingApi api) throws GeocachingApiException {
    AuthenticatorHelper authenticatorHelper = App.get(mContext).getAuthenticatorHelper();

    String token = authenticatorHelper.getAuthToken();
    if (token == null) {
      authenticatorHelper.removeAccount();
      throw new InvalidCredentialsException("Account not found.");
    }

    api.openSession(token);
  }


  private void removeCachesOverDistance(@NonNull List<Geocache> caches, @NonNull Coordinates coordinates, double maxDistance) {
    while (caches.size() > 0) {
      Geocache cache = caches.get(caches.size() - 1);
      double distance = computeDistance(coordinates, cache);

      if (distance > maxDistance) {
        Timber.i("Cache " + cache.getCode() + " is over distance.");
        caches.remove(cache);
      } else {
        return;
      }
    }
  }

  public float getDistance() {
    boolean imperialUnits = mPrefs.getBoolean(PrefConstants.IMPERIAL_UNITS, false);

    double distance = PreferenceUtil.getParsedDouble(mPrefs, PrefConstants.FILTER_DISTANCE,
        imperialUnits ? 31.0685596 : 50);
    if (imperialUnits) {
      distance *= AppConstants.MILES_PER_KILOMETER;
    }

    // fix for min and max distance error in Geocaching Live API
    return (float) Math.max(Math.min(distance, 50), 0.1);
  }

  private double computeDistance(@NonNull Coordinates coordinates, @NonNull Geocache cache) {
    return cache.getCoordinates().distanceTo(coordinates) / 1000;
  }

  private int computeCachesPerRequest(int currentCachesPerRequest, long requestDuration) {
    int cachesPerRequest = currentCachesPerRequest;

    // keep the request time between ADAPTIVE_DOWNLOADING_MIN_TIME_MS and ADAPTIVE_DOWNLOADING_MAX_TIME_MS
    if (requestDuration < AppConstants.ADAPTIVE_DOWNLOADING_MIN_TIME_MS)
      cachesPerRequest+= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

    if (requestDuration > AppConstants.ADAPTIVE_DOWNLOADING_MAX_TIME_MS)
      cachesPerRequest-= AppConstants.ADAPTIVE_DOWNLOADING_STEP;

    // keep the value in a range
    cachesPerRequest = Math.max(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MIN_CACHES);
    cachesPerRequest = Math.min(cachesPerRequest, AppConstants.ADAPTIVE_DOWNLOADING_MAX_CACHES);

    return cachesPerRequest;
  }
}
