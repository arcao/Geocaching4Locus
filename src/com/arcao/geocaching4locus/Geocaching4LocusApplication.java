package com.arcao.geocaching4locus;

import geocaching.api.data.SimpleGeocache;

import java.util.ArrayList;
import java.util.List;

import android.app.Application;

public class Geocaching4LocusApplication extends Application {

	private List<SimpleGeocache> list = new ArrayList<SimpleGeocache>();
	
	@Override
	public void onCreate() {
		super.onCreate();

		
		// prepare list
		// load temp list
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		
		// save temp list
	}
	
	public List<SimpleGeocache> getList() {
		return list;
	}
}
