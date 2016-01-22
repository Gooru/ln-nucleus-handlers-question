package org.gooru.nucleus.handlers.questions.processors.events;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 19/1/16.
 */
public class EventBuilderFactory {

  private static final String EVT_ASSESSMENT_CREATE = "event.assessment.create";
  private static final String EVT_ASSESSMENT_UPDATE = "event.assessment.update";
  private static final String EVT_ASSESSMENT_DELETE = "event.assessment.delete";
  private static final String EVT_ASSESSMENT_COPY = "event.assessment.copy";
  private static final String EVENT_NAME = "event.name";
  private static final String EVENT_BODY = "event.body";
  private static final String ASSESSMENT_ID = "id";

  public static EventBuilder getDeleteAssessmentEventBuilder(String assessmentId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_ASSESSMENT_DELETE).put(EVENT_BODY, new JsonObject().put(ASSESSMENT_ID, assessmentId));
  }

  public static EventBuilder getCreateAssessmentEventBuilder(String assessmentId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_ASSESSMENT_CREATE).put(EVENT_BODY, new JsonObject().put(ASSESSMENT_ID, assessmentId));
  }

  public static EventBuilder getUpdateAssessmentEventBuilder(String assessmentId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_ASSESSMENT_UPDATE).put(EVENT_BODY, new JsonObject().put(ASSESSMENT_ID, assessmentId));
  }

  public static EventBuilder getCopyAssessmentEventBuilder(String assessmentId) {
    return () -> new JsonObject().put(EVENT_NAME, EVT_ASSESSMENT_COPY).put(EVENT_BODY, new JsonObject().put(ASSESSMENT_ID, assessmentId));
  }
}
