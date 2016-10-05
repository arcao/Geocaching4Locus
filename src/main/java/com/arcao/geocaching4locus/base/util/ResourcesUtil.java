package com.arcao.geocaching4locus.base.util;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import android.text.Html;
import android.text.SpannedString;

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
  public static CharSequence getText(@NonNull Context context, @StringRes int id, Object... args) {
    return HtmlUtil.fromHtml(getHtmlString(context, id, args));
  }

  /**
   * Returns HTML string from string resource
   * @param context application context
   * @param id string resource id
   * @param args arguments for String.format(...)
   * @return HTML string
   */
  public static String getHtmlString(@NonNull Context context, @StringRes int id, Object... args) {
    return String.format(Html.toHtml(new SpannedString(context.getText(id))), args);
  }
}
