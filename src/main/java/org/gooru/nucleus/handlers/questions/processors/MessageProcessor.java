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

class MessageProcessor implements Processor {

  private static final Logger LOGGER = LoggerFactory.getLogger(Processor.class);
  private String userId;
  private JsonObject prefs;
  private JsonObject request;
  private final Message<Object> message;

  public MessageProcessor(Message<Object> message) {
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

  private MessageResponse processQuestionUpdate() {
    ProcessorContext context = createContext();
    if (context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.error("Invalid request, question id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid question id");
    }
    return new RepoBuilder().buildQuestionRepo(context).updateQuestion();
  }

  private MessageResponse processQuestionGet() {
    ProcessorContext context = createContext();
    if (context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.error("Invalid request, question id not available. Aborting");
      return MessageResponseFactory.createInvalidRequestResponse("Invalid question id");
    }
    return new RepoBuilder().buildQuestionRepo(context).fetchQuestion();
  }

  private MessageResponse processQuestionCreate() {
    ProcessorContext context = createContext();

    return new RepoBuilder().buildQuestionRepo(context).createQuestion();
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
    if (userId == null) {
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

}
