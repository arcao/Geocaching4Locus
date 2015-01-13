package com.arcao.geocaching4locus.util.feedback.collector;

import java.lang.reflect.Field;

public class ConstantsCollector extends Collector {
	private final String prefix;
	private final Class<?> source;

	public ConstantsCollector(Class<?> source, String prefix) {
		this.prefix = prefix;
		this.source = source;
	}

	@Override
	public String getName() {
		return prefix + " CONSTANTS";
	}

	@Override
	public String collect() {
		final StringBuilder result = new StringBuilder();

		final Field[] fields = source.getFields();
		for (final Field field : fields) {
			result.append(field.getName()).append("=");
			try {
				result.append(field.get(null).toString());
			} catch (IllegalArgumentException | IllegalAccessException e) {
				result.append("N/A");
			}
			result.append("\n");
		}

		return result.toString();
	}
}
