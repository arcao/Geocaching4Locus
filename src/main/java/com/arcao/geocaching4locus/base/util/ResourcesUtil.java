package com.arcao.geocaching4locus.base.util;

import android.content.Context;
import android.text.Html;
import android.text.SpannedString;
import android.text.TextUtils;

/**
 * Created by Arcao on 27.09.2016.
 */

public final class ResourcesUtil {
  private ResourcesUtil() {}

  /**
   * Returns formatted text from string resources
   * @param context application context
   * @param id string resource id
   * @param args arguments for String.format(...)
   * @return formatted SpannedString
   */
  public static CharSequence getText(Context context, int id, Object... args) {
    final int len = args.length;

    for(int i = 0; i < len; i++)
      args[i] = args[i] instanceof String? TextUtils.htmlEncode((String)args[i]) : args[i];

    return HtmlUtil.fromHtml(String.format(Html.toHtml(new SpannedString(context.getText(id))), args));
  }
}
