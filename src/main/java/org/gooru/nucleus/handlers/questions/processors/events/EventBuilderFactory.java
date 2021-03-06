package org.gooru.nucleus.handlers.questions.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public final class EventBuilderFactory {

  private static final String EVT_QUESTION_CREATE = "event.question.create";
  private static final String EVT_QUESTION_UPDATE = "event.question.update";
  private static final String EVT_QUESTION_DELETE = "event.question.delete";
  private static final String EVT_RUBRIC_CREATE = "event.rubric.create";
  private static final String EVT_RUBRIC_UPDATE = "event.rubric.update";
  private static final String EVT_RUBRIC_DELETE = "event.rubric.delete";
  private static final String EVT_QUESTION_RUBRIC_ASSOCIATE = "question.rubirc.association";
  private static final String EVENT_NAME = "event.name";
  private static final String EVENT_BODY = "event.body";
  private static final String ID = "id";
  private static final String RUBRIC_ID = "rubric_id";
  private static final String QUESTION_ID = "question_id";

  private EventBuilderFactory() {
    throw new AssertionError();
  }

  public static EventBuilder getDeleteQuestionEventBuilder(String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_DELETE).put(EVENT_BODY,
        new JsonObject().put(ID, questionId));
  }

  public static EventBuilder getCreateQuestionEventBuilder(String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_CREATE).put(EVENT_BODY,
        new JsonObject().put(ID, questionId));
  }

  public static EventBuilder getUpdateQuestionEventBuilder(String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_UPDATE).put(EVENT_BODY,
        new JsonObject().put(ID, questionId));
  }

  public static EventBuilder getDeleteRubricEventBuilder(String rubricId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_RUBRIC_DELETE).put(EVENT_BODY,
        new JsonObject().put(ID, rubricId));
  }

  public static EventBuilder getCreateRubricEventBuilder(String rubricId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_RUBRIC_CREATE).put(EVENT_BODY,
        new JsonObject().put(ID, rubricId));
  }

  public static EventBuilder getUpdateRubricEventBuilder(String rubricId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_RUBRIC_UPDATE).put(EVENT_BODY,
        new JsonObject().put(ID, rubricId));
  }

  public static EventBuilder getAssociateRubricWithQuestionEventBuilder(String rubricId,
      String questionId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_QUESTION_RUBRIC_ASSOCIATE).put(EVENT_BODY,
        new JsonObject().put(RUBRIC_ID, rubricId).put(QUESTION_ID, questionId));
  }

}
