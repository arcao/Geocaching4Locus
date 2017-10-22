package com.arcao.feedback.collector;

import android.content.Context;
import locus.api.android.ActionTools;
import locus.api.android.utils.LocusInfo;
import locus.api.android.utils.LocusUtils;

public class LocusMapInfoCollector extends Collector {
	private final Context mContext;

	public LocusMapInfoCollector(Context context) {
		this.mContext = context;
	}

	@Override
	public String getName() {
		return "LocusMapInfo";
	}

	@Override
	protected String collect() {
		StringBuilder sb = new StringBuilder();

		try {
			LocusUtils.LocusVersion lv = LocusUtils.getActiveVersion(mContext);
			if (lv != null) {
				sb.append("Locus Version = ").append(lv.getVersionName());
				sb.append("\nLocus Package = ").append(lv.getPackageName());

				LocusInfo info = ActionTools.getLocusInfo(mContext, lv);
				if (info != null) {
					sb.append("\nLocus info:\n").append(info.toString());
				}
			} else {
				sb.append("Locus not installed!");
			}
		} catch (Throwable t) {
			sb.append("Unable to get info from Locus Map:\n").append(throwableToString(t));
		}

		return sb.toString();
	}
}
