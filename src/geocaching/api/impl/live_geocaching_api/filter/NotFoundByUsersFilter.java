package geocaching.api.impl.live_geocaching_api.filter;

import google.gson.stream.JsonWriter;

import java.io.IOException;

public class NotFoundByUsersFilter implements Filter {
	private static final String NAME = "NotFoundByUsers";
	
	protected String[] userNames;
	
	public NotFoundByUsersFilter(String... userNames) {
		this.userNames = userNames;
	}
	
	public String[] getUserNames() {
		return userNames;
	}
	
	@Override
	public boolean isValid() {
		if (userNames == null || userNames.length == 0)
			return false;
		
		boolean valid = false;
		for (String userName : userNames) {
			if (userName != null && userName.length() > 0)
				valid = true;
		}
		
		return valid;
	}

	@Override
	public void writeJson(JsonWriter w) throws IOException {
		w.name(NAME);
		w.beginObject();
		w.name("UserNames");
		w.beginArray();
		for (String userName : userNames) {
			if (userName != null && userName.length() > 0)
				w.value(userName);
		}
		w.endArray();
		w.endObject(); 
	}

	@Override
	public String getName() {
		return NAME;
	}

}
