package org.gooru.nucleus.handlers.questions.processors;

import io.vertx.core.json.JsonObject;

public interface Processor {
  public JsonObject process();
}
