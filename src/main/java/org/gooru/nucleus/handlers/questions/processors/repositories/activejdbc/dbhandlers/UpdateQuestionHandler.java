package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.PayloadValidator;
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
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
  private final ProcessorContext context;
  private AJEntityQuestion question;

  UpdateQuestionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a question id present
    if (context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.warn("Missing question id");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.question.id")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // The user should not be anonymous
    if (context.userId() == null || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      return new ExecutionResult<>(
          MessageResponseFactory
              .createForbiddenResponse(RESOURCE_BUNDLE.getString("anonymous.user")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Payload should not be empty
    if (context.request() == null || context.request().isEmpty()) {
      LOGGER.warn("Empty payload supplied to edit question");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("payload.empty")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    JsonObject errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityQuestion.editFieldSelector(),
            AJEntityQuestion.getValidatorRegistry());
    if (errors != null && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityQuestion> questions = AJEntityQuestion
        .findBySQL(AJEntityQuestion.VALIDATE_EXISTS_NON_DELETED, AJEntityQuestion.QUESTION,
            context.questionId(),
            false);
    // Question should be present in DB
    if (questions.size() < 1) {
      LOGGER.warn("Question id: {} not present in DB", context.questionId());
      return new ExecutionResult<>(MessageResponseFactory
          .createNotFoundResponse(RESOURCE_BUNDLE.getString("question.id") + context.questionId()),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    this.question = questions.get(0);
    if (!authorized()) {
      // Update is forbidden
      return new ExecutionResult<>(MessageResponseFactory
          .createForbiddenResponse(
              RESOURCE_BUNDLE.getString("not.owner.collaborator.on.course.collection")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // Now override auto populate values
    autoPopulate();
    new DefaultAJEntityQuestionEntityBuilder()
        .build(question, context.request(), AJEntityQuestion.getConverterRegistry());

    boolean result = question.save();
    if (!result) {
      LOGGER.error("Question with id '{}' failed to save", context.questionId());
      if (question.hasErrors()) {
        Map<String, String> map = question.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }

    return new ExecutionResult<>(MessageResponseFactory
        .createNoContentResponse(RESOURCE_BUNDLE.getString("updated"),
            EventBuilderFactory.getUpdateQuestionEventBuilder(this.context.questionId())),
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
    if (creator != null && creator.equalsIgnoreCase(context.userId()) && course == null
        && collection == null) {
      // Since the creator is modifying, and it is not part of any
      // collection or course, then owner should be able to modify
      return true;
    } else {
      // The ownership and rights flows from either collection or course
      long authRecordCount;
      if (course != null) {
        // Check if user is one of collaborator on course, we do not
        // need to check the owner as course owner should be question
        // creator
        authRecordCount =
            Base.count(AJEntityQuestion.TABLE_COURSE, AJEntityQuestion.AUTH_FILTER, course,
                context.userId(),
                context.userId());
        if (authRecordCount >= 1) {
          // Auth check successful
          LOGGER.debug("Auth check successful based on course: {}", course);
          return true;
        }
      } else if (collection != null) {
        // Check if the user is one of collaborator on collection, we do
        // not need to check about course now
        authRecordCount =
            Base.count(AJEntityQuestion.TABLE_COLLECTION, AJEntityQuestion.AUTH_FILTER, collection,
                context.userId(), context.userId());
        if (authRecordCount >= 1) {
          LOGGER.debug("Auth check successful based on collection: {}", collection);
          return true;
        }
      }
    }

    return false;
  }

  private void autoPopulate() {
    this.question.setModifierId(this.context.userId());
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityQuestionEntityBuilder implements
      EntityBuilder<AJEntityQuestion> {

  }

}
