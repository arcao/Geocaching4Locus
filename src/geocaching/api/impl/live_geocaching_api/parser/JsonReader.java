package geocaching.api.impl.live_geocaching_api.parser;

import google.gson.stream.JsonToken;

import java.io.IOException;
import java.io.Reader;

public class JsonReader {
	google.gson.stream.JsonReader r;
	
	public JsonReader(Reader in) {
		r = new google.gson.stream.JsonReader(in);
	}

	/**
	 * Configure this parser to be  be liberal in what it accepts. By default,
	 * this parser is strict and only accepts JSON as specified by <a
	 * href="http://www.ietf.org/rfc/rfc4627.txt">RFC 4627</a>. Setting the
	 * parser to lenient causes it to ignore the following syntax errors:
	 *
	 * <ul>
	 *   <li>Streams that start with the <a href="#nonexecuteprefix">non-execute
	 *       prefix</a>, <code>")]}'\n"</code>.
	 *   <li>Streams that include multiple top-level values. With strict parsing,
	 *       each stream must contain exactly one top-level value.
	 *   <li>Top-level values of any type. With strict parsing, the top-level
	 *       value must be an object or an array.
	 *   <li>Numbers may be {@link Double#isNaN() NaNs} or {@link
	 *       Double#isInfinite() infinities}.
	 *   <li>End of line comments starting with {@code //} or {@code #} and
	 *       ending with a newline character.
	 *   <li>C-style comments starting with {@code /*} and ending with
	 *       {@code *}{@code /}. Such comments may not be nested.
	 *   <li>Names that are unquoted or {@code 'single quoted'}.
	 *   <li>Strings that are unquoted or {@code 'single quoted'}.
	 *   <li>Array elements separated by {@code ;} instead of {@code ,}.
	 *   <li>Unnecessary array separators. These are interpreted as if null
	 *       was the omitted value.
	 *   <li>Names and values separated by {@code =} or {@code =>} instead of
	 *       {@code :}.
	 *   <li>Name/value pairs separated by {@code ;} instead of {@code ,}.
	 * </ul>
	 */
	public void setLenient(boolean lenient) {
		r.setLenient(lenient);
	}

	/**
	 * Returns true if this parser is liberal in what it accepts.
	 */
	public boolean isLenient() {
		return r.isLenient();
	}

	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * beginning of a new array.
	 */
	public void beginArray() throws IOException {
		r.beginArray();
	}

	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * end of the current array.
	 */
	public void endArray() throws IOException {
		r.endArray();
	}

	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * beginning of a new object.
	 */
	public void beginObject() throws IOException {
		r.beginObject();
	}

	/**
	 * Consumes the next token from the JSON stream and asserts that it is the
	 * end of the current array.
	 */
	public void endObject() throws IOException {
		r.endObject();
	}

	/**
	 * Returns true if the current array or object has another element.
	 */
	public boolean hasNext() throws IOException {
		return r.hasNext();
	}

	/**
	 * Returns the type of the next token without consuming it.
	 */
	public JsonToken peek() throws IOException {
		return r.peek();
	}

	/**
	 * Returns the next token, a {@link JsonToken#NAME property name}, and
	 * consumes it.
	 *
	 * @throws IOException if the next token in the stream is not a property
	 *     name.
	 */
	public String nextName() throws IOException {
		return r.nextName();
	}

	/**
	 * Returns the {@link JsonToken#STRING string} value of the next token,
	 * consuming it. If the next token is a number, this method will return its
	 * string form.
	 *
	 * @throws IllegalStateException if the next token is not a string or if
	 *     this reader is closed.
	 */
	public String nextString() throws IOException {
		return nextString("");
	}
	
	public String nextString(String defaultValue) throws IOException {
		if (r.peek() == JsonToken.NULL) {
			r.skipValue();
			return defaultValue;
		}
		
		return r.nextString();
	}

	/**
	 * Returns the {@link JsonToken#BOOLEAN boolean} value of the next token,
	 * consuming it.
	 *
	 * @throws IllegalStateException if the next token is not a boolean or if
	 *     this reader is closed.
	 */
	public boolean nextBoolean() throws IOException {
		return nextBoolean(false);
	}

	public boolean nextBoolean(boolean defaultValue) throws IOException {
		if (r.peek() == JsonToken.NULL) {
			r.skipValue();
			return defaultValue;
		}
		
		return r.nextBoolean();
	}

	/**
	 * Consumes the next token from the JSON stream and asserts that it is a
	 * literal null.
	 *
	 * @throws IllegalStateException if the next token is not null or if this
	 *     reader is closed.
	 */
	public void nextNull() throws IOException {
		r.nextNull();
	}

	/**
	 * Returns the {@link JsonToken#NUMBER double} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as a double.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a double, or is non-finite.
	 */
	public double nextDouble() throws IOException {
		return nextDouble(0D);
	}
	
	public double nextDouble(double defaultValue) throws IOException {
		if (r.peek() == JsonToken.NULL) {
			r.skipValue();
			return defaultValue;
		}
		
		return r.nextDouble();
	}

	/**
	 * Returns the {@link JsonToken#NUMBER long} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as a long. If the next token's numeric value cannot be exactly
	 * represented by a Java {@code long}, this method throws.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a number, or exactly represented as a long.
	 */
	public long nextLong() throws IOException {
		return nextLong(0L);
	}
	
	public long nextLong(long defaultValue) throws IOException {
		if (r.peek() == JsonToken.NULL) {
			r.skipValue();
			return defaultValue;
		}
		
		return r.nextLong();
	}

	/**
	 * Returns the {@link JsonToken#NUMBER int} value of the next token,
	 * consuming it. If the next token is a string, this method will attempt to
	 * parse it as an int. If the next token's numeric value cannot be exactly
	 * represented by a Java {@code int}, this method throws.
	 *
	 * @throws IllegalStateException if the next token is not a literal value.
	 * @throws NumberFormatException if the next literal value cannot be parsed
	 *     as a number, or exactly represented as an int.
	 */
	public int nextInt() throws IOException {
		return nextInt(0);
	}
	
	public int nextInt(int defaultValue) throws IOException {
		if (r.peek() == JsonToken.NULL) {
			r.skipValue();
			return defaultValue;
		}
		
		return r.nextInt();
	}

	/**
	 * Closes this JSON reader and the underlying {@link Reader}.
	 */
	public void close() throws IOException {
		r.close();
	}

	/**
	 * Skips the next value recursively. If it is an object or array, all nested
	 * elements are skipped. This method is intended for use when the JSON token
	 * stream contains unrecognized or unhandled values.
	 */
	public void skipValue() throws IOException {
		r.skipValue();
	}

	@Override
	public String toString() {
		return r.toString();
	}
}
