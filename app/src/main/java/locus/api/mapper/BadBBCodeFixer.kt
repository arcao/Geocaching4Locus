package locus.api.mapper

import java.util.regex.Pattern

internal object BadBBCodeFixer {
    private val BBCODE_PATTERN_FLAGS = Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    private val BBCODE_MAP = mapOf(
        Pattern.compile("\\[align=(.+?)](.+?)\\[/align=[^]]+]", BBCODE_PATTERN_FLAGS) to "<div align='$1'>$2</div>",
        Pattern.compile("\\[color=(.+?)](.+?)\\[/color=[^]]+]", BBCODE_PATTERN_FLAGS) to "<font color='$1'>$2</font>",
        Pattern.compile("\\[size=(.+?)](.+?)\\[/size=[^]]+]", BBCODE_PATTERN_FLAGS) to "<font size='$1'>$2</font>",
        Pattern.compile("\\[img=(.+?),(.+?)](.+?)\\[/img=[^]]+]", BBCODE_PATTERN_FLAGS) to "<img width='$1' height='$2' src='$3' />",
        Pattern.compile("\\[email=(.+?)](.+?)\\[/email=[^]]+]", BBCODE_PATTERN_FLAGS) to "<a href='mailto:$1'>$2</a>",
        Pattern.compile("\\[url=(.+?)](.+?)\\[/url=[^]]+]", BBCODE_PATTERN_FLAGS) to "<a href='$1'>$2</a>"
    )

    @JvmStatic
    fun fix(text: String?): String? {
        if (text == null)
            return null

        var html: String = text

        for ((key, value) in BBCODE_MAP) {
            html = key.matcher(html).replaceAll(value)
        }

        return html
    }
}
