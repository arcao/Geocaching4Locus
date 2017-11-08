package com.arcao.feedback.collector;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class MemoryCollector extends Collector {

    @Override
    public String getName() {
        return "MEMORY";
    }

    @Override
    protected String collect() {
        final StringBuilder memInfo = new StringBuilder();

        try {
            final List<String> commandLine = new ArrayList<>();
            commandLine.add("dumpsys");
            commandLine.add("meminfo");
            commandLine.add(Integer.toString(android.os.Process.myPid()));

            final Process process = Runtime.getRuntime().exec(commandLine.toArray(new String[commandLine.size()]));
            final BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));

            while (true) {
                final String line = bufferedReader.readLine();
                if (line == null) {
                    break;
                }
                memInfo.append(line);
                memInfo.append("\n");
            }

        } catch (IOException e) {
            Timber.e(e, "MemoryCollector could not retrieve data");
        }

        return memInfo.toString();

    }
}
