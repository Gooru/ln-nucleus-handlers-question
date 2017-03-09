package org.gooru.nucleus.handlers.questions.processors.utils;

import java.util.Iterator;

import io.vertx.core.json.JsonArray;

/**
 * @author szgooru
 * Created On: 02-Mar-2017
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
        for (;;) {
            String s = it.next().toString();
            sb.append(s);
            if (!it.hasNext()) {
                return sb.append('}').toString();
            }
            sb.append(',');
        }
    }
}
