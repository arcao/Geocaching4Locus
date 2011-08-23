package geocaching.api.impl.live_geocaching_api.parser;

import geocaching.api.impl.live_geocaching_api.StatusCode;

import java.io.IOException;

public class StatusJsonParser {

	public static Status parse(JsonReader r) throws IOException {
		Status status = new Status();
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("StatusCode".equals(name)) {
				status.statusCode = StatusCode.parseStatusCode(r.nextInt());
			} else if ("StatusMessage".equals(name)) {
				status.statusMessage = r.nextString();
			} else if ("ExceptionDetails".equals(name)) {
				status.exceptionDetails = r.nextString();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		return status;
	}
		
	public static class Status {
		protected StatusCode statusCode;
		protected String statusMessage;
		protected String exceptionDetails;
				
		public StatusCode getStatusCode() {
			return statusCode;
		}
		
		public String getStatusMessage() {
			return statusMessage;
		}
		
		public String getExceptionDetails() {
			return exceptionDetails;
		}
	}
}
