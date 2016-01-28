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
import org.postgresql.util.PGobject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

/**
 * Created by ashish on 26/1/16.
 */
public class DeleteQuestionHandler implements DBHandler {
  private final ProcessorContext context;
  private static final Logger LOGGER = LoggerFactory.getLogger(DeleteQuestionHandler.class);
  private AJEntityQuestion question;

  public DeleteQuestionHandler(ProcessorContext context) {
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
    setPGObject(AJEntityQuestion.MODIFIER_ID, AJEntityQuestion.UUID_TYPE, this.context.userId());
    if (this.question.hasErrors()) {
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }

    this.question.setBoolean(AJEntityQuestion.IS_DELETED, true);

    if (!this.question.save()) {
      LOGGER.debug("Save errors");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()), ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(MessageResponseFactory
      .createNoContentResponse("Deleted successfully", EventBuilderFactory.getDeleteQuestionEventBuilder(this.context.questionId())),
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
        authRecordCount =
          Base.count(AJEntityQuestion.TABLE_COURSE, AJEntityQuestion.AUTH_VIA_COURSE_FILTER, course, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          // Auth check successful
          LOGGER.debug("Auth check successful based on course: {}", course);
          return true;
        }
      } else if (collection != null) {
        // Check if the user is one of collaborator on collection, we do not need to check about course now
        authRecordCount =
          Base.count(AJEntityQuestion.TABLE_COLLECTION, AJEntityQuestion.AUTH_VIA_COLLECTION_FILTER, collection, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          LOGGER.debug("Auth check successful based on collection: {}", collection);
          return true;
        }
      }
    }

    return false;
  }

  private void setPGObject(String field, String type, String value) {
    PGobject pgObject = new PGobject();
    pgObject.setType(type);
    try {
      pgObject.setValue(value);
      this.question.set(field, pgObject);
    } catch (SQLException e) {
      LOGGER.error("Not able to set value for field: {}, type: {}, value: {}", field, type, value);
      this.question.errors().put(field, value);
    }
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.question.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }
}
