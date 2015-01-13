package com.arcao.geocaching4locus.util.feedback.collector;

public abstract class Collector {
	public abstract String getName();
	public abstract String collect();

	@Override
	public String toString() {
		return "--- " + getName() + " ---\n" + collect() + "\n------\n\n";
	}
}
