package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by ashish on 11/1/16.
 */
class CreateQuestionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(CreateQuestionHandler.class);

  private final ProcessorContext context;
  private AJEntityQuestion question;

  public CreateQuestionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a question id present
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Invalid request payload");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Invalid request payload"),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    JsonObject errors = validateForbiddenFields();
    if (errors != null && !errors.isEmpty()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors), ExecutionResult.ExecutionStatus.FAILED);
    }
    if (context.userId() == null || context.userId().isEmpty() || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Anonymous user denied this action"),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    this.question = new AJEntityQuestion();

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    this.question.setAllFromJson(context.request());
    // Now override auto populate values
    autoPopulate();
    if (this.question.hasErrors()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }
    if (!this.question.isValid()) {
      LOGGER.debug("Validation errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (!this.question.save()) {
      LOGGER.debug("Save errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(MessageResponseFactory
      .createCreatedResponse(this.question.getId().toString(), EventBuilderFactory.getCreateQuestionEventBuilder(this.question.getId().toString())),
      ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject validateForbiddenFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    AJEntityQuestion.INSERT_QUESTION_FORBIDDEN_FIELDS.stream().filter(invalidField -> input.getValue(invalidField) != null)
                                                     .forEach(invalidField -> output.put(invalidField, "Field not allowed"));
    return output.isEmpty() ? null : output;
  }

  private void autoPopulate() {
    this.question.setModifierId(context.userId());
    this.question.setCreatorId(context.userId());
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.question.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

}
