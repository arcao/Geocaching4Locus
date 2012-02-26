package com.arcao.geocaching4locus;

import org.apache.log4j.Level;

import android.app.Application;
import de.mindpipe.android.logging.log4j.LogConfigurator;

public class Geocaching4LocusApplication extends Application {
	// configure log4j
	static {
    final LogConfigurator logConfigurator = new LogConfigurator();
    
    logConfigurator.setUseFileAppender(false);
    logConfigurator.setUseLogCatAppender(true);
    logConfigurator.setRootLevel(Level.INFO);
    logConfigurator.setLevel("com.arcao", Level.DEBUG);
    logConfigurator.configure();
	}
}
