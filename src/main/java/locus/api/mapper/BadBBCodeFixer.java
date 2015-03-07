package locus.api.mapper;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class BadBBCodeFixer {
	private static final Map<Pattern, String> BBCODE_MAP = new HashMap<>();
	private static final int BBCODE_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE | Pattern.DOTALL;

	static {
		BBCODE_MAP.put(Pattern.compile("\\[align=(.+?)\\](.+?)\\[/align=[^\\]]+\\]", BBCODE_PATTERN_FLAGS), "<div align='$1'>$2</div>");
		BBCODE_MAP.put(Pattern.compile("\\[color=(.+?)\\](.+?)\\[/color=[^\\]]+\\]", BBCODE_PATTERN_FLAGS), "<font color='$1'>$2</font>");
		BBCODE_MAP.put(Pattern.compile("\\[size=(.+?)\\](.+?)\\[/size=[^\\]]+\\]", BBCODE_PATTERN_FLAGS), "<font size='$1'>$2</font>");
		BBCODE_MAP.put(Pattern.compile("\\[img=(.+?),(.+?)\\](.+?)\\[/img=[^\\]]+\\]", BBCODE_PATTERN_FLAGS), "<img width='$1' height='$2' src='$3' />");
		BBCODE_MAP.put(Pattern.compile("\\[email=(.+?)\\](.+?)\\[/email=[^\\]]+\\]", BBCODE_PATTERN_FLAGS), "<a href='mailto:$1'>$2</a>");
		BBCODE_MAP.put(Pattern.compile("\\[url=(.+?)\\](.+?)\\[/url=[^\\]]+\\]", BBCODE_PATTERN_FLAGS), "<a href='$1'>$2</a>");
	}

	public static String fix(String text) {
		if (text == null)
			return null;

		String html = text;

		for (Map.Entry<Pattern, String> entry: BBCODE_MAP.entrySet()) {
			html = entry.getKey().matcher(html).replaceAll(entry.getValue());
		}

		return html;
	}
}
