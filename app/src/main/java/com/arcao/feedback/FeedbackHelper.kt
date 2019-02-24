package com.arcao.feedback

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import com.arcao.feedback.collector.Collector
import com.arcao.geocaching4locus.App
import com.arcao.geocaching4locus.base.coroutine.CoroutinesDispatcherProvider
import kotlinx.coroutines.withContext
import org.koin.standalone.KoinComponent
import org.koin.standalone.inject
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.Stack
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// TODO convert to class
object FeedbackHelper : KoinComponent {
    private val app by inject<App>()
    private val collectors by inject<List<Collector>>(DEP_FEEDBACK_COLLECTORS)
    private val dispatcherProvider by inject<CoroutinesDispatcherProvider>()

    @JvmStatic
    suspend fun sendFeedback(@NonNull activity: Activity, @StringRes resEmail: Int, @StringRes resSubject: Int, @StringRes resMessageText: Int) =
        withContext(dispatcherProvider.computation) {
            val subject = activity.getString(resSubject, app.name, app.version)

            val email = activity.getString(resEmail)

            val intent = Intent(Intent.ACTION_SEND).apply {
                // only e-mail apps
                type = "plain/text"
                putExtra(Intent.EXTRA_SUBJECT, subject)
                putExtra(Intent.EXTRA_TEXT, activity.getString(resMessageText))
                putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            try {
                createReport(FeedbackFileProvider.getReportFile(activity))

                val reportUri = FeedbackFileProvider.reportFileUri
                intent.putExtra(Intent.EXTRA_STREAM, reportUri)

                // grant read permission
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    val resInfoList = activity.packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

                    for (resolveInfo in resInfoList) {
                        val packageName = resolveInfo.activityInfo.packageName
                        activity.grantUriPermission(packageName, reportUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                }
            } catch (e: IOException) {
                Timber.e(e)
            }

            withContext(dispatcherProvider.main) {
                activity.startActivity(createEmailOnlyChooserIntent(activity, intent, null))
            }
        }

    @Throws(IOException::class)
    private suspend fun createReport(reportFile: File) {
        if (reportFile.exists()) {
            Timber.d("Report file $reportFile already exist.")
            if (reportFile.delete()) {
                Timber.d("Report file removed.")
            }
        }

        Timber.d("Creating report to %s", reportFile)
        ZipOutputStream(FileOutputStream(reportFile)).use { zos -> writeCollectors(zos) }
        Timber.d("Report created.")
    }

    @Throws(IOException::class)
    private suspend fun writeCollectors(zos: ZipOutputStream) {
        for (collector in collectors) {
            zos.putNextEntry(ZipEntry(collector.name + ".txt"))

            OutputStreamWriter(zos, "UTF-8").apply {
                write(
                    try {
                        collector()
                    } catch (ignored: Exception) {
                        // ignored
                        ""
                    }
                )
                flush()
            }

            zos.closeEntry()
        }
    }

    private fun createEmailOnlyChooserIntent(context: Context, source: Intent, chooserTitle: CharSequence?): Intent {
        val intents = Stack<Intent>()
        val emailIntent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:info@domain.com"))

        context.packageManager.queryIntentActivities(emailIntent, 0).forEach { resolveInfo ->
            intents.add(Intent(source).setPackage(resolveInfo.activityInfo.packageName))
        }

        return if (!intents.isEmpty()) {
            Intent.createChooser(intents.removeAt(0), chooserTitle)
                .putExtra(Intent.EXTRA_INITIAL_INTENTS, intents.toTypedArray<Parcelable>())
        } else {
            Intent.createChooser(source, chooserTitle)
        }
    }
}
