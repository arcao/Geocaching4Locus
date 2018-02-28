package com.arcao.feedback.collector;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import timber.log.Timber;

public class LogCatCollector extends Collector {
    private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;
    private static final String[] COMMAND_LINE = {"logcat", "-t", "10000", "-v", "time"};

    private final Context context;

    public LogCatCollector(Context context) {
        this.context = context.getApplicationContext();
    }

    @Override
    public String getName() {
        return "LOGCAT";
    }

    @Override
    protected String collect() {
        if (!hasReadLogPermission()) {
            return "N/A";
        }

        final StringBuilder buffer = new StringBuilder();

        try {
            final Process process =
                    Runtime.getRuntime().exec(COMMAND_LINE);
            try (BufferedReader bufferedReader = new BufferedReader(
                    new InputStreamReader(process.getInputStream()))) {

                Timber.d("Retrieving logcat output...");
                // Dump stderr to null
                new Thread(() -> {
                    try (InputStream stderr = process.getErrorStream()) {
                        byte[] dummy = new byte[DEFAULT_BUFFER_SIZE_IN_BYTES];
                        //noinspection StatementWithEmptyBody
                        while (stderr.read(dummy) >= 0) ; // discard all data
                    } catch (IOException e) {
                        // fall trough
                    }
                }).start();

                String line;
                while ((line = bufferedReader.readLine()) != null) {
                    buffer.append(line).append("\n");
                }
            }
        } catch (IOException e) {
            Timber.e("LogCatCollector could not retrieve data.");
        }

        return buffer.toString();
    }

    private boolean hasReadLogPermission() {
        final PackageManager pm = context.getPackageManager();
        if (pm == null) {
            return false;
        }

        try {
            return pm.checkPermission(Manifest.permission.READ_LOGS, context.getPackageName())
                    == PackageManager.PERMISSION_GRANTED;
        } catch (RuntimeException e) {
            // To catch RuntimeException("Package manager has died") that can occur on some version of Android,
            // when the remote PackageManager is unavailable. I suspect this sometimes occurs when the App is being reinstalled.
            return false;
        }
    }
}
