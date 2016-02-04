package org.gooru.nucleus.handlers.questions.processors;

import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

class MessageProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private final Message<Object> message;
  private String userId;
  private JsonObject prefs;
  private JsonObject request;

  MessageProcessor(Message<Object> message) {
    this.message = message;
  }

  @Override
  public MessageResponse process() {
    MessageResponse result;
    try {
      // Validate the message itself
      ExecutionResult<MessageResponse> validateResult = validateAndInitialize();
      if (validateResult.isCompleted()) {
        return validateResult.result();
      }

      final String msgOp = message.headers().get(MessageConstants.MSG_HEADER_OP);
      switch (msgOp) {
        case MessageConstants.MSG_OP_QUESTION_CREATE:
          result = processQuestionCreate();
          break;
        case MessageConstants.MSG_OP_QUESTION_GET:
          result = processQuestionGet();
          break;
        case MessageConstants.MSG_OP_QUESTION_UPDATE:
          result = processQuestionUpdate();
          break;
        case MessageConstants.MSG_OP_QUESTION_DELETE:
          result = processQuestionDelete();
          break;
        default:
          LOGGER.error("Invalid operation type passed in, not able to handle");
          return MessageResponseFactory.createInvalidRequestResponse("Invalid operation");
      }
      return result;
    } catch (Throwable e) {
      LOGGER.error("Unhandled exception in processing", e);
      return MessageResponseFactory.createInternalErrorResponse();
    }

  }

  private MessageResponse processQuestionDelete() {
    ProcessorContext context = createContext();
    if (isIdInvalid(context)) {
      return MessageResponseFactory.createInvalidRequestResponse("Invalid question id");
    }
    return RepoBuilder.buildQuestionRepo(context).deleteQuestion();
  }

  private MessageResponse processQuestionUpdate() {
    ProcessorContext context = createContext();
    if (isIdInvalid(context)) {
      return MessageResponseFactory.createInvalidRequestResponse("Invalid question id");
    }
    return RepoBuilder.buildQuestionRepo(context).updateQuestion();
  }

  private MessageResponse processQuestionGet() {
    ProcessorContext context = createContext();
    if (isIdInvalid(context)) {
      return MessageResponseFactory.createInvalidRequestResponse("Invalid question id");
    }
    return RepoBuilder.buildQuestionRepo(context).fetchQuestion();
  }

  private MessageResponse processQuestionCreate() {
    ProcessorContext context = createContext();

    return RepoBuilder.buildQuestionRepo(context).createQuestion();
  }


  private ProcessorContext createContext() {
    String questionId = message.headers().get(MessageConstants.QUESTION_ID);

    return new ProcessorContext(userId, prefs, request, questionId);
  }

  private ExecutionResult<MessageResponse> validateAndInitialize() {
    if (message == null || !(message.body() instanceof JsonObject)) {
      LOGGER.error("Invalid message received, either null or body of message is not JsonObject ");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    userId = ((JsonObject) message.body()).getString(MessageConstants.MSG_USER_ID);
    if (!validateUser(userId)) {
      LOGGER.error("Invalid user id passed. Not authorized.");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }
    prefs = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_KEY_PREFS);
    request = ((JsonObject) message.body()).getJsonObject(MessageConstants.MSG_HTTP_BODY);

    if (prefs == null || prefs.isEmpty()) {
      LOGGER.error("Invalid preferences obtained, probably not authorized properly");
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (request == null) {
      LOGGER.error("Invalid JSON payload on Message Bus");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(), ExecutionResult.ExecutionStatus.FAILED);
    }

    // All is well, continue processing
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  private boolean validateUser(String userId) {
    return !(userId == null || userId.isEmpty()) && (userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS) || validateUuid(userId));
  }

  private boolean isIdInvalid(ProcessorContext context) {
    if (context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.error("Invalid request, question id not available. Aborting");
      return true;
    }
    return !validateUuid(context.questionId());
  }

  private boolean validateUuid(String uuidString) {
    try {
      UUID uuid = UUID.fromString(uuidString);
      return true;
    } catch (IllegalArgumentException e) {
      LOGGER.error("Invalid request, id is not a valid uuid. Aborting");
      return false;
    } catch (Exception e) {
      LOGGER.error("Invalid request, id is not a valid uuid. Aborting");
      return false;
    }
  }


}
