package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 11/1/16.
 */
class UpdateQuestionHandler implements DBHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(UpdateQuestionHandler.class);
  private final ProcessorContext context;
  private AJEntityQuestion question;

  public UpdateQuestionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a question id present
    if (context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.warn("Missing question id");
      return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse("Missing question id"),
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
    LazyList<AJEntityQuestion> questions =
      AJEntityQuestion.findBySQL(AJEntityQuestion.VALIDATE_EXISTS_NON_DELETED, AJEntityQuestion.QUESTION, context.questionId(), false);
    // Question should be present in DB
    if (questions.size() < 1) {
      LOGGER.warn("Question id: {} not present in DB", context.questionId());
      return new ExecutionResult<>(MessageResponseFactory.createNotFoundResponse("question id: " + context.questionId()),
        ExecutionResult.ExecutionStatus.FAILED);
    }

    this.question = questions.get(0);
    if (!authorized()) {
      // Update is forbidden
      return new ExecutionResult<>(MessageResponseFactory.createForbiddenResponse("Need to be owner/collaborator on course/collection"),
        ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // Populate the model with new values
    this.question.setAllFromJson(this.context.request());
    // Now override auto populate values
    autoPopulate();
    if (!this.question.isValid()) {
      LOGGER.debug("Validation errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }

    if (!this.question.save()) {
      LOGGER.debug("Save errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(MessageResponseFactory
      .createNoContentResponse("Updated successfully", EventBuilderFactory.getUpdateQuestionEventBuilder(this.context.questionId())),
      ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private boolean authorized() {
    String creator = question.getString(AJEntityQuestion.CREATOR_ID);
    String course = question.getString(AJEntityQuestion.COURSE_ID);
    String collection = question.getString(AJEntityQuestion.COLLECTION_ID);
    if (creator != null && creator.equalsIgnoreCase(context.userId()) && course == null && collection == null) {
      // Since the creator is modifying, and it is not part of any collection or course, then owner should be able to modify
      return true;
    } else {
      // The ownership and rights flows from either collection or course
      long authRecordCount;
      if (course != null) {
        // Check if user is one of collaborator on course, we do not need to check the owner as course owner should be question creator
        authRecordCount = Base.count(AJEntityQuestion.TABLE_COURSE, AJEntityQuestion.AUTH_FILTER, course, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          // Auth check successful
          LOGGER.debug("Auth check successful based on course: {}", course);
          return true;
        }
      } else if (collection != null) {
        // Check if the user is one of collaborator on collection, we do not need to check about course now
        authRecordCount = Base.count(AJEntityQuestion.TABLE_COLLECTION, AJEntityQuestion.AUTH_FILTER, collection, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          LOGGER.debug("Auth check successful based on collection: {}", collection);
          return true;
        }
      }
    }

    return false;
  }

  private JsonObject validateForbiddenFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    AJEntityQuestion.UPDATE_QUESTION_FORBIDDEN_FIELDS.stream().filter(invalidField -> input.getValue(invalidField) != null)
                                                     .forEach(invalidField -> output.put(invalidField, "Field not allowed"));
    return output.isEmpty() ? null : output;
  }

  private void autoPopulate() {
    this.question.setModifierId(this.context.userId());
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.question.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }


}
