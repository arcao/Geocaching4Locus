package locus.api.manager

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Environment
import com.arcao.geocaching4locus.error.exception.LocusMapRuntimeException
import locus.api.android.ActionBasics
import locus.api.android.ActionDisplayInternal
import locus.api.android.ActionDisplayPoints
import locus.api.android.ActionDisplayPointsExtended
import locus.api.android.ActionTools
import locus.api.android.objects.PackPoints
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
                    ActionTools.getLocusInfo(context, locusVersion)?.isPeriodicUpdatesEnabled ?: false
                } catch (e: Throwable) {
                    Timber.e(e, "Unable to receive info about periodic update state from Locus Map.")
                    return true
                }
            } else {
                return false
            }
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

    fun sendPointsFile(callImport: Boolean, center: Boolean, intentFlags: Int): Boolean =
        sendPointsFile(LocusConst.ACTION_DISPLAY_DATA, callImport, center, intentFlags)

    @Throws(LocusMapRuntimeException::class)
    private fun sendPointsFile(
        action: String,
        callImport: Boolean,
        center: Boolean,
        intentFlags: Int
    ): Boolean {
        try {
            val file = cacheFile

            if (!file.exists())
                return false

            val intent = Intent()
                .addFlags(intentFlags)
                .putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.absolutePath)

            return ActionDisplayInternal.sendData(action, context, intent, callImport, center)
        } catch (e: Exception) {
            throw LocusMapRuntimeException(e)
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
            val locusVersion = LocusUtils.getActiveVersion(context)
            ActionTools.updateLocusWaypoint(context, locusVersion, point, false)
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }
    }

    fun getPoint(pointIndex: Long): Point? {
        try {
            val locusVersion = LocusUtils.getActiveVersion(context)
            return ActionBasics.getPoint(context, locusVersion, pointIndex)
        } catch (t: Throwable) {
            throw LocusMapRuntimeException(t)
        }
    }

    companion object {
        private const val APP_DIRECTORY = "Geocaching4Locus"
        private const val CACHE_FILENAME = "data.locus"

        fun createSendPointsIntent(callImport: Boolean, center: Boolean): Intent {
            return Intent(LocusConst.ACTION_DISPLAY_DATA)
                .putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, cacheFile)
                // set centering tag
                .putExtra(LocusConst.INTENT_EXTRA_CENTER_ON_DATA, center)
                // set import tag
                .putExtra(LocusConst.INTENT_EXTRA_CALL_IMPORT, callImport)
        }

        /**
         * Get a path including file name to save data for Locus
         *
         * @return path to file
         */
        val cacheFile: File
            get() {
                val cacheFile = File(
                    File(
                        Environment.getExternalStorageDirectory(),
                        APP_DIRECTORY
                    ), CACHE_FILENAME
                )

                Timber.d("Cache file for Locus: %s", cacheFile.toString())

                val parentDirectory = cacheFile.parentFile

                if (!parentDirectory.mkdirs()) {
                    Timber.w("Directory '%s' not created.", parentDirectory)
                }

                if (!parentDirectory.isDirectory)
                    throw IllegalStateException("External storage (or SD Card) is not writable.")

                return cacheFile
            }

        /**
         * Get a OutputFileStream to save data for Locus
         *
         * @return OutputFileStream object for world readable file returned by getCacheFileName method
         * @throws IOException If I/O error occurs
         */
        // create empty file
        // file has to be readable for Locus
        val cacheFileOutputStream: FileOutputStream
            @SuppressLint("SetWorldReadable")
            @Throws(IOException::class)
            get() {
                val file = ActionDisplayPointsExtended.cacheFileName
                val fos = FileOutputStream(file)
                fos.flush()
                if (!file.setReadable(true, false)) {
                    Timber.e("Unable to set readable all for: %s", file)
                }

                return fos
            }
    }
}