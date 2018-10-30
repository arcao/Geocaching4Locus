package com.arcao.feedback

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Parcelable
import androidx.annotation.NonNull
import androidx.annotation.StringRes
import com.arcao.feedback.collector.*
import timber.log.Timber
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStreamWriter
import java.util.*
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

// TODO convert to class
object FeedbackHelper {
    @JvmStatic
    fun sendFeedback(@NonNull context: Context, @StringRes resEmail: Int, @StringRes resSubject: Int, @StringRes resMessageText: Int) {
        val subject = context.getString(resSubject, getApplicationName(context), getVersion(context))

        val email = context.getString(resEmail)

        val intent = Intent(Intent.ACTION_SEND).apply {
            // only e-mail apps
            type = "plain/text"
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, context.getString(resMessageText))
            putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        try {
            createReport(context, FeedbackFileProvider.getReportFile(context))

            val reportUri = FeedbackFileProvider.reportFileUri
            intent.putExtra(Intent.EXTRA_STREAM, reportUri)

            // grant read permission
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            } else {
                val resInfoList = context.packageManager
                        .queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)

                for (resolveInfo in resInfoList) {
                    val packageName = resolveInfo.activityInfo.packageName
                    context.grantUriPermission(packageName, reportUri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
            }
        } catch (e: IOException) {
            Timber.e(e)
        }

        context.startActivity(createEmailOnlyChooserIntent(context, intent, null))
    }

    @Throws(IOException::class)
    private fun createReport(context: Context, reportFile: File) {
        if (reportFile.exists()) {
            Timber.d("Report file $reportFile already exist.")
            if (reportFile.delete()) {
                Timber.d("Report file removed.")
            }
        }

        Timber.d("Creating report to %s", reportFile)
        ZipOutputStream(FileOutputStream(reportFile)).use { zos -> writeCollectors(zos, context) }
        Timber.d("Report created.")
    }

    @Throws(IOException::class)
    private fun writeCollectors(zos: ZipOutputStream, context: Context) {
        val collectors = prepareCollectors(context)

        for (collector in collectors) {
            zos.putNextEntry(ZipEntry(collector.name + ".txt"))

            OutputStreamWriter(zos, "UTF-8").apply {
                write(collector.toString())
                flush()
            }

            zos.closeEntry()
        }
    }

    private fun prepareCollectors(context: Context): Collection<Collector> {
        return listOf(
                AppInfoCollector(context),
                BuildConfigCollector(),
                ConfigurationCollector(context),
                ConstantsCollector(Build::class.java, "BUILD"),
                ConstantsCollector(Build.VERSION::class.java, "VERSION"),
                MemoryCollector(),
                SharedPreferencesCollector(context),
                DisplayManagerCollector(context),
                AccountInfoCollector(context),
                LocusMapInfoCollector(context),

                // LogCat collector has to be the latest one to receive exceptions from collectors
                LogCatCollector()
        )
    }

    private fun getApplicationName(context: Context): String {
        return context.getString(context.applicationInfo.labelRes)
    }

    private fun getVersion(context: Context): String {
        return try {
            context.packageManager.getPackageInfo(context.packageName, 0).versionName
        } catch (e: PackageManager.NameNotFoundException) {
            Timber.e(e)
            "0.0"
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
