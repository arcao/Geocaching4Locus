package geocaching.api.data;

import geocaching.api.data.type.LogType;

import java.lang.reflect.Method;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import menion.android.locus.addon.publiclib.geoData.PointGeocachingDataLog;

public class CacheLog {
	private final Date date;
	private final LogType logType;
	private final String author;
	private final String text;

	private static final DateFormat GPX_TIME_FMT = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

	public CacheLog(Date date, LogType logType, String author, String text) {
		this.date = date;
		this.logType = logType;
		this.author = author;
		this.text = text;
	}

	public Date getDate() {
		return date;
	}

	public LogType getLogType() {
		return logType;
	}

	public String getAuthor() {
		return author;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		for (Method m : getClass().getMethods()) {
			if (!m.getName().startsWith("get") ||
					m.getParameterTypes().length != 0 ||
					void.class.equals(m.getReturnType()))
				continue;

			sb.append(m.getName());
			sb.append(':');
			try {
				sb.append(m.invoke(this, new Object[0]));
			} catch (Exception e) {
			}
			sb.append("\n");
		}
		return sb.toString();
	}

	public PointGeocachingDataLog toPointGeocachingDataLog() {
		PointGeocachingDataLog p = new PointGeocachingDataLog();

		p.date = GPX_TIME_FMT.format(date);
		p.finder = author;
		// p.finderFound
		p.logText = text;
		p.type = logType.getId();

		return p;
	}
}
