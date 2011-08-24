package geocaching.api.impl.live_geocaching_api.builder;

import java.util.Date;
import java.util.Locale;

public class JsonBuilder {
	public static String dateToJsonString(Date date) {
    if (date == null)
    	throw new NullPointerException("Date cannot be null.");
    
    return String.format(Locale.US, "\\/Date(%d)\\/", date.getTime());
  }
}
