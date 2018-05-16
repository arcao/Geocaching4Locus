package com.arcao.feedback.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringWriter;

import timber.log.Timber;

public class LogCatCollector extends Collector {
    private static final int DEFAULT_BUFFER_SIZE_IN_BYTES = 8192;
    private static final String[] COMMAND_LINE = {"logcat", "-t", "10000", "-v", "time"};

    @Override
    public String getName() {
        return "LOGCAT";
    }

    @Override
    protected String collect() {
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
        } catch (Throwable t) {
            Timber.e(t, "LogCatCollector could not retrieve data.");

            StringWriter sw = new StringWriter();
            t.printStackTrace(new PrintWriter(sw));
            return "Error: " + sw.toString();
        }

        return buffer.toString();
    }
}
