package com.arcao.feedback

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.content.pm.ProviderInfo
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import android.provider.OpenableColumns
import android.webkit.MimeTypeMap
import androidx.annotation.NonNull
import com.arcao.geocaching4locus.BuildConfig
import timber.log.Timber
import java.io.File
import java.io.FileNotFoundException

/**
 * Provider for attaching feedback file to 3rd application:
 *
 * Usage in AndroidManifest.xml file:
 * ```xml
 * <provider
 *     android:name="com.arcao.feedback.FeedbackFileProvider"
 *     android:authorities="${applicationId}.provider.feedback"
 *     android:exported="true"
 *     android:enabled="true"
 *     android:grantUriPermissions="true"/>
 * ```
 */
class FeedbackFileProvider : ContentProvider() {

    // UriMatcher used to match against incoming requests
    private lateinit var uriMatcher: UriMatcher
    private lateinit var reportFile: File

    override fun onCreate(): Boolean {
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH)
        uriMatcher.addURI(AUTHORITY, REPORT_FILE_NAME, REPORT_FILE_ID)

        return true
    }

    override fun attachInfo(context: Context, info: ProviderInfo) {
        super.attachInfo(context, info)

        // Sanity check our security
        if (info.exported) {
            throw SecurityException("Provider must not be exported")
        }
        if (!info.grantUriPermissions) {
            throw SecurityException("Provider must grant uri permissions")
        }

        reportFile = getReportFile(context)
    }

    @Throws(FileNotFoundException::class)
    override fun openFile(@NonNull uri: Uri, @NonNull mode: String): ParcelFileDescriptor {
        Timber.v("openFile: Called with uri: '$uri'.")

        // Check incoming Uri against the matcher
        when (uriMatcher.match(uri)) {
            REPORT_FILE_ID -> {
                if (!reportFile.exists()) {
                    Timber.e("File '$reportFile' for uri '$uri' not found")
                    throw FileNotFoundException(reportFile.toString())
                }

                return ParcelFileDescriptor.open(reportFile, ParcelFileDescriptor.MODE_READ_ONLY)
            }

            else -> {
                Timber.e("Unsupported uri: '$uri'.")
                throw FileNotFoundException("Unsupported uri: $uri")
            }
        }
    }

    override fun getType(@NonNull uri: Uri): String {
        Timber.v("getType: Called with uri: '$uri'")

        val fileName = uri.lastPathSegment.orEmpty()

        val lastDot = fileName.lastIndexOf('.')
        if (lastDot >= 0) {
            val extension = fileName.substring(lastDot + 1)
            val mime = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension)
            if (mime != null) {
                return mime
            }
        }

        return "application/octet-stream"
    }

    override fun query(
        @NonNull uri: Uri, projection: Array<String>?,
        s: String,
        as1: Array<String>,
        s1: String
    ): Cursor? {
        assert(context != null)

        when (uriMatcher.match(uri)) {
            REPORT_FILE_ID -> {
                val columns = projection ?: COLUMNS
                var cols = arrayOfNulls<String>(columns.size)
                var values = arrayOfNulls<Any>(columns.size)

                var i = 0
                for (col in columns) {
                    if (OpenableColumns.DISPLAY_NAME == col) {
                        cols[i] = OpenableColumns.DISPLAY_NAME
                        values[i] = reportFile.name
                        i++
                    } else if (OpenableColumns.SIZE == col) {
                        cols[i] = OpenableColumns.SIZE
                        values[i] = reportFile.length()
                        i++
                    }
                }

                cols = cols.copyOf(i)
                values = values.copyOf(i)

                val cursor = MatrixCursor(cols, 1)
                cursor.addRow(values)
                return cursor
            }
            else -> return null
        }
    }

    // Not supported / used / methods
    override fun update(@NonNull uri: Uri, contentvalues: ContentValues, s: String, `as`: Array<String>): Int {
        throw UnsupportedOperationException("No external updates")
    }

    override fun delete(@NonNull uri: Uri, s: String, `as`: Array<String>): Int {
        throw UnsupportedOperationException("No external deletes")
    }

    override fun insert(@NonNull uri: Uri, contentvalues: ContentValues): Uri {
        throw UnsupportedOperationException("No external inserts")
    }

    companion object {
        private val COLUMNS = arrayOf(OpenableColumns.DISPLAY_NAME, OpenableColumns.SIZE)
        private const val AUTHORITY = BuildConfig.APPLICATION_ID + ".provider.feedback"
        private const val REPORT_FILE_NAME = "logs.zip"
        private const val REPORT_FILE_ID = 1

        @JvmStatic
        fun getReportFile(@NonNull context: Context): File {
            return File(context.cacheDir, REPORT_FILE_NAME)
        }

        @JvmStatic
        val reportFileUri: Uri
            get() = Uri.Builder().scheme("content").authority(AUTHORITY).path(REPORT_FILE_NAME).build()
    }
}
