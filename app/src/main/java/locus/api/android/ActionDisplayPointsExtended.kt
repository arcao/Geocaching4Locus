package locus.api.android

import android.content.Context
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import locus.api.manager.LocusMapManager
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ActionDisplayPointsExtended : ActionDisplayPoints() {
    private const val APP_DIRECTORY = "Geocaching4Locus"
    private const val CACHE_FILENAME = "data.locus"

    @JvmStatic
    @Deprecated("Use LocusMapManager", ReplaceWith("locusMapManager.sendPointsFile(callImport, center, intentFlags)"))
    @Throws(RequiredVersionMissingException::class)
    fun sendPacksFile(context: Context, file: File, callImport: Boolean, center: Boolean, intentFlags: Int) =
        LocusMapManager(context).sendPointsFile(callImport, center, intentFlags)

    @JvmStatic
    @Deprecated("Use LocusMapManager.", ReplaceWith("locusMapManager.createSendPackIntent(callImport, center)"))
    fun createSendPacksIntent(file: File, callImport: Boolean, center: Boolean) = LocusMapManager.createSendPointsIntent(callImport, center)

    /**
     * Get a path including file name to save data for Locus
     *
     * @return path to file
     */
    @JvmStatic
    @Deprecated("Use LocusMapManager", ReplaceWith("LocusMapManager.cacheFile"))
    val cacheFileName: File = LocusMapManager.cacheFile

    /**
     * Get a OutputFileStream to save data for Locus
     *
     * @return OutputFileStream object for world readable file returned by getCacheFileName method
     * @throws IOException If I/O error occurs
     */
    // create empty file
    // file has to be readable for Locus
    @JvmStatic
    @Deprecated("Use LocusMapManager", ReplaceWith("LocusMapManager.cacheFileOutputStream"))
    val cacheFileOutputStream: FileOutputStream = LocusMapManager.cacheFileOutputStream
}
