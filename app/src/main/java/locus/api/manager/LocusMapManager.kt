package locus.api.manager

import android.content.BroadcastReceiver
import android.content.ClipData
import android.content.Context
import android.content.Intent
import androidx.core.content.FileProvider
import com.arcao.geocaching4locus.base.constants.AppConstants
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException
import locus.api.android.ActionBasics
import locus.api.android.ActionDisplayPoints
import locus.api.android.ActionTools
import locus.api.android.objects.PackPoints
import locus.api.android.utils.IntentHelper
import locus.api.android.utils.LocusConst
import locus.api.android.utils.LocusUtils
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.objects.extra.Point
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import kotlin.reflect.KClass

class LocusMapManager(
        private val context: Context
) {
    val periodicUpdateEnabled: Boolean
        get() {
            val locusVersion = LocusUtils.getActiveVersion(context)
            return if (locusVersion != null) {
                try {
                    ActionBasics.getLocusInfo(context, locusVersion)?.isPeriodicUpdatesEnabled
                            ?: false
                } catch (e: Throwable) {
                    Timber.e(e, "Unable to receive info about periodic update state from Locus Map.")
                    return true
                }
            } else {
                return false
            }
        }

    val isLocusMapNotInstalled: Boolean
        get() {
            val lv = LocusUtils.getActiveVersion(context)
            return lv == null || !lv.isVersionValid(AppConstants.LOCUS_MIN_VERSION_CODE)
        }


    fun createSendPointsIntent(callImport: Boolean, center: Boolean): Intent {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.provider", cacheFile)

        return Intent(LocusConst.ACTION_DISPLAY_DATA).apply {
            putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_URI, uri)
            // set centering tag
            putExtra(LocusConst.INTENT_EXTRA_CENTER_ON_DATA, center)
            // set import tag
            putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport)
            clipData = ClipData.newRawUri("", uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
    }

    /**
     * Get a path including file name to save data for Locus
     *
     * @return path to file
     */
    private val cacheFile: File
        get() {
            val cacheFile = File(File(context.cacheDir, "export"), CACHE_FILENAME)

            Timber.d("Cache file for Locus: %s", cacheFile.toString())

            val parentDirectory = cacheFile.parentFile

            if (!parentDirectory.mkdirs()) {
                Timber.w("Directory '%s' not created, maybe exists.", parentDirectory)
            }

            if (!parentDirectory.isDirectory)
                throw IllegalStateException("Directory $parentDirectory not exist.")

            return cacheFile
        }

    /**
     * Get a OutputFileStream to save data for Locus Map
     *
     * @return OutputFileStream object
     * @throws IOException If I/O error occurs
     */
    val cacheFileOutputStream: FileOutputStream
        @Throws(IOException::class)
        get() {
            val file = cacheFile

            // make sure the path exist
            file.parentFile.mkdirs()

            val fos = FileOutputStream(file)
            fos.flush()
            return fos
        }


    /**
     * Allows to remove already send Pack from the map. Keep in mind, that this method remove
     * only packs that are visible (temporary) on map.
     *
     * @param packName name of pack
     * @throws LocusMapRuntimeException exception in case ane exception occurs while Locus Map API calls
     */
    @Throws(RequiredVersionMissingException::class)
    fun removePackFromLocus(packName: String) {
        try {
            // create empty pack
            val pw = PackPoints(packName)

            // create and send intent
            val intent = Intent()
            intent.putExtra(LocusConst.INTENT_EXTRA_POINTS_DATA, pw.asBytes)
            ActionDisplayPoints.sendPackSilent(context, pw, false)
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }
    }

    // make sure Live Map broadcast receiver is always enabled
    fun <T : BroadcastReceiver> enablePeriodicUpdatesReceiver(clazz: KClass<T>) {
        try {
            val locusVersion = LocusUtils.getActiveVersion(context)
            if (locusVersion != null) {
                ActionTools.enablePeriodicUpdatesReceiver(
                        context,
                        locusVersion,
                        clazz.java
                )
            }
        } catch (e: Throwable) {
            Timber.e(e, "Unable to enable ${clazz.java.simpleName}.")
        }
    }

    @Throws(LocusMapRuntimeException::class)
    fun sendPointsSilent(packName: String, points: List<Point>, centerOnData: Boolean = false) {
        try {
            val pack = PackPoints(packName)
            points.forEach(pack::addWaypoint)
            ActionDisplayPoints.sendPackSilent(context, pack, centerOnData)
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }
    }

    fun updatePoint(point: Point) {
        try {
            val locusVersion = LocusUtils.getActiveVersion(context) ?: return
            ActionBasics.updatePoint(context, locusVersion, point, false)
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }
    }

    fun getPoint(pointIndex: Long): Point? {
        try {
            val locusVersion = LocusUtils.getActiveVersion(context) ?: return null
            return ActionBasics.getPoint(context, locusVersion, pointIndex)
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }
    }

    fun isIntentPointTools(intent: Intent) = IntentHelper.isIntentPointTools(intent)

    fun getPointFromIntent(intent: Intent) = IntentHelper.getPointFromIntent(context, intent)

    fun getLocationFromIntent(intent: Intent) =
            IntentHelper.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_GPS)
                    ?: IntentHelper.getLocationFromIntent(intent, LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)

    fun isLocationIntent(intent: Intent) = intent.hasExtra(LocusConst.INTENT_EXTRA_LOCATION_MAP_CENTER)

    companion object {
        private const val CACHE_FILENAME = "data.locus"
    }
}