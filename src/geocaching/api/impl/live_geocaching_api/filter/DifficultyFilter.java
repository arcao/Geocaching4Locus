package geocaching.api.impl.live_geocaching_api.filter;

import google.gson.stream.JsonWriter;

import java.io.IOException;

public class DifficultyFilter implements Filter {
	private static final String NAME = "Difficulty";
	
	protected final float min;
	protected final float max;
	
	public DifficultyFilter(float min, float max) {
		this.min = min;
		this.max = max;
	}
	
	@Override
	public String getName() {
		return NAME;
	}
	
	public float getMin() {
		return min;
	}
	
	public float getMax() {
		return max;
	}

	@Override
	public boolean isValid() {
		return min != 1 || max != 5;
	}
	
	@Override
	public void writeJson(JsonWriter w) throws IOException {
		w.name(NAME);
		w.beginObject();
		
		w.name("MinDifficulty").value(min);
		w.name("MaxDifficulty").value(max);
		
		w.endObject();
	}
}
