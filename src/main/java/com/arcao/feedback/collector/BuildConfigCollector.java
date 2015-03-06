package com.arcao.feedback.collector;

import com.arcao.geocaching4locus.BuildConfig;

public class BuildConfigCollector extends Collector {
	@Override
	public String getName() {
		return "BuildConfig INFO";
	}

	@Override
	protected String collect() {
		final StringBuilder sb = new StringBuilder();

		sb.append("APPLICATION_ID=").append(BuildConfig.APPLICATION_ID).append("\n");
		sb.append("BUILD_TIME=").append(BuildConfig.BUILD_TIME).append("\n");
		sb.append("BUILD_TYPE=").append(BuildConfig.BUILD_TYPE).append("\n");
		sb.append("DEBUG=").append(BuildConfig.DEBUG).append("\n");
		sb.append("FLAVOR=").append(BuildConfig.FLAVOR).append("\n");
		sb.append("GEOCACHING_API_STAGING=").append(BuildConfig.GEOCACHING_API_STAGING).append("\n");
		sb.append("GIT_SHA=").append(BuildConfig.GIT_SHA).append("\n");
		sb.append("VERSION_CODE=").append(BuildConfig.VERSION_CODE).append("\n");
		sb.append("VERSION_NAME=").append(BuildConfig.VERSION_NAME).append("\n");

		return sb.toString();
	}
}
