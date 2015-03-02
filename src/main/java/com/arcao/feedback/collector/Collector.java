package com.arcao.feedback.collector;

public abstract class Collector {
	public abstract String getName();
	protected abstract String collect();

	@Override
	public String toString() {
		return "--- " + getName() + " ---\n" + collect() + "\n------\n\n";
	}
}
