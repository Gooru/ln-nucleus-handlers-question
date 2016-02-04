package org.gooru.nucleus.handlers.questions.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public final class EventBuilderFactory {

  private static final String EVT_QUESTION_CREATE = "event.question.create";
  private static final String EVT_QUESTION_UPDATE = "event.question.update";
  private static final String EVT_QUESTION_DELETE = "event.question.delete";
  private static final String EVT_QUESTION_COPY = "event.question.copy";
  private static final String EVENT_NAME = "event.name";
  private static final String EVENT_BODY = "event.body";
  private static final String QUESTION_ID = "id";

  private EventBuilderFactory() {
    throw new AssertionError();
  }

  public static EventBuilder getDeleteQuestionEventBuilder(String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_DELETE).put(EVENT_BODY, new JsonObject().put(QUESTION_ID, questionId));
  }

  public static EventBuilder getCreateQuestionEventBuilder(String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_CREATE).put(EVENT_BODY, new JsonObject().put(QUESTION_ID, questionId));
  }

  public static EventBuilder getUpdateQuestionEventBuilder(String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_UPDATE).put(EVENT_BODY, new JsonObject().put(QUESTION_ID, questionId));
  }

}
