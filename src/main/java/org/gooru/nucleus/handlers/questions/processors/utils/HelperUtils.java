package org.gooru.nucleus.handlers.questions.processors.utils;

import io.vertx.core.json.JsonArray;
import java.util.Collection;
import java.util.Iterator;

/**
 * @author szgooru Created On: 02-Mar-2017
 */
public final class HelperUtils {

  private HelperUtils() {
    throw new AssertionError();
  }

  public static String toPostgresTextArrayFromJsonArray(JsonArray input) {
    Iterator<Object> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder();
    sb.append('{');
    for (; ; ) {
      String s = it.next().toString();
      sb.append(s);
      if (!it.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',');
    }
  }

  public static String toPostgresArrayString(Collection<String> input) {
    int approxSize = ((input.size() + 1) * 36); // Length of UUID is around
    // 36
    // chars
    Iterator<String> it = input.iterator();
    if (!it.hasNext()) {
      return "{}";
    }

    StringBuilder sb = new StringBuilder(approxSize);
    sb.append('{');
    for (; ; ) {
      String s = it.next();
      sb.append('"').append(s).append('"');
      if (!it.hasNext()) {
        return sb.append('}').toString();
      }
      sb.append(',');
    }
  }
}
