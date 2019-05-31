package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers.helpers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;

/**
 * @author szgooru Created On: 02-Mar-2017
 */
public final class TaxonomyHelper {

  private TaxonomyHelper() {
    throw new AssertionError();
  }

  public static JsonArray populateGutCodes(JsonObject request) {
    JsonObject taxonomy = request.getJsonObject(AJEntityRubric.TAXONOMY);
    if (taxonomy != null && !taxonomy.isEmpty()) {
      JsonArray result = new JsonArray();
      taxonomy.fieldNames().forEach(result::add);
      //request.put(AJEntityRubric.GUT_CODES, result);
      return result;
    }

    return null;
  }

}
