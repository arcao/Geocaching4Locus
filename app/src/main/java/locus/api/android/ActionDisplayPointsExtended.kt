package locus.api.android

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Environment
import androidx.annotation.Nullable
import locus.api.android.utils.LocusConst
import locus.api.android.utils.exceptions.RequiredVersionMissingException
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ActionDisplayPointsExtended : ActionDisplayPoints() {
    private const val LOCUS_CACHE_FILENAME = "data.locus"

    @JvmStatic
    @Throws(RequiredVersionMissingException::class)
    fun sendPacksFile(context: Context, file: File, callImport: Boolean, center: Boolean, intentFlags: Int): Boolean {
        return sendPacksFile(LocusConst.ACTION_DISPLAY_DATA, context, file, callImport, center,
                intentFlags)
    }

    @JvmStatic
    @Throws(RequiredVersionMissingException::class)
    private fun sendPacksFile(
        action: String,
        context: Context,
        file: File,
        callImport: Boolean,
        center: Boolean,
        intentFlags: Int
    ): Boolean {
        if (!file.exists())
            return false

        val intent = Intent()
                .addFlags(intentFlags)
                .putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.absolutePath)

        return ActionDisplay.sendData(action, context, intent, callImport, center)
    }

    @JvmStatic
    @Nullable
    fun createSendPacksIntent(file: File, callImport: Boolean, center: Boolean): Intent? {
        if (!file.exists())
            return null

        return Intent(LocusConst.ACTION_DISPLAY_DATA)
                .putExtra(LocusConst.INTENT_EXTRA_POINTS_FILE_PATH, file.absolutePath)
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
    @JvmStatic
    val cacheFileName: File
        get() {
            val cacheFile = File(Environment.getExternalStorageDirectory(), String.format("/Geocaching4Locus/%s", LOCUS_CACHE_FILENAME))

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
    @JvmStatic
    val cacheFileOutputStream: FileOutputStream
        @SuppressLint("SetWorldReadable")
        @Throws(IOException::class)
        get() {
            val file = cacheFileName
            val fos = FileOutputStream(file)
            fos.flush()
            if (!file.setReadable(true, false)) {
                Timber.e("Unable to set readable all for: %s", file)
            }

            return fos
        }
}
