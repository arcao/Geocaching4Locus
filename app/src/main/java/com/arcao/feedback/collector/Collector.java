package com.arcao.feedback.collector;

import java.io.PrintWriter;
import java.io.StringWriter;

public abstract class Collector {
	public abstract String getName();
	protected abstract String collect();

	@Override
	public String toString() {
		return "--- " + getName() + " ---\n" + collect() + "\n------\n\n";
	}

	protected String throwableToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		pw.close();
		return sw.toString();
	}
}
