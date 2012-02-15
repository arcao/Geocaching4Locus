package com.arcao.wherigoservice.api.parser;

import geocaching.api.impl.live_geocaching_api.parser.JsonReader;

import java.io.IOException;

public class WherigoJsonResultParser {
	public static Result parse(JsonReader r) throws IOException {
		Result status = new Result();
		r.beginObject();
		while(r.hasNext()) {
			String name = r.nextName();
			if ("Code".equals(name)) {
				status.statusCode = r.nextInt();
			} else if ("Text".equals(name)) {
				status.statusMessage = r.nextString();
			} else {
				r.skipValue();
			}
		}
		r.endObject();
		return status;
	}
	
	public static class Result {
		protected int statusCode;
		protected String statusMessage;
				
		public int getStatusCode() {
			return statusCode;
		}
		
		public String getStatusMessage() {
			return statusMessage;
		}
	}
}
