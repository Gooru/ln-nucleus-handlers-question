package org.gooru.nucleus.handlers.questions.constants;

public final class MessageConstants {

  public static final String MSG_HEADER_OP = "mb.operation";
  public static final String MSG_HEADER_TOKEN = "session.token";
  public static final String MSG_OP_STATUS = "mb.operation.status";
  public static final String MSG_KEY_SESSION = "session";
  public static final String MSG_OP_STATUS_SUCCESS = "success";
  public static final String MSG_OP_STATUS_ERROR = "error";
  public static final String MSG_OP_STATUS_VALIDATION_ERROR = "error.validation";
  public static final String MSG_USER_ANONYMOUS = "anonymous";
  public static final String MSG_USER_ID = "user_id";
  public static final String MSG_HTTP_STATUS = "http.status";
  public static final String MSG_HTTP_BODY = "http.body";
  public static final String MSG_HTTP_RESPONSE = "http.response";
  public static final String MSG_HTTP_ERROR = "http.error";
  public static final String MSG_HTTP_VALIDATION_ERROR = "http.validation.error";
  public static final String MSG_HTTP_HEADERS = "http.headers";
  public static final String MSG_MESSAGE = "message";

  // Operation names: Also need to be updated in corresponding handlers
  public static final String MSG_OP_QUESTION_GET = "question.get";
  public static final String MSG_OP_QUESTION_CREATE = "question.create";
  public static final String MSG_OP_QUESTION_UPDATE = "question.update";
  public static final String MSG_OP_QUESTION_DELETE = "question.delete";
  public static final String MSG_OP_QUESTION_GET_BULK = "bulk.question.get";
  public static final String MSG_OP_QUESTION_SCORE_UPDATE = "question.score.update";

  public static final String MSG_OP_RUBRIC_GET = "rubric.get";
  public static final String MSG_OP_RUBRIC_CREATE = "rubric.create";
  public static final String MSG_OP_RUBRIC_UPDATE = "rubric.update";
  public static final String MSG_OP_RUBRIC_DELETE = "rubric.delete";
  public static final String MSG_OP_QUESTION_RUBRIC_ASSOCIATE = "question.rubirc.association";

  // Containers for different responses
  public static final String RESP_CONTAINER_MBUS = "mb.container";
  public static final String RESP_CONTAINER_EVENT = "mb.event";

  public static final String QUESTION_ID = "questionId";
  public static final String RUBRIC_ID = "rubricId";

  private MessageConstants() {
    throw new AssertionError();
  }
}
