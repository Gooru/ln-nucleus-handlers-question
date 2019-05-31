package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.util.ResourceBundle;
import java.util.UUID;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.SanityChecker;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 27-Feb-2017
 */
public class AssociateRubricWithQuestionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory
      .getLogger(AssociateRubricWithQuestionHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private final ProcessorContext context;
  private AJEntityQuestion question;
  private AJEntityRubric rubric;
  private UUID copiedRubricId;
  private AJEntityRubric associatingRubric;

  public AssociateRubricWithQuestionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    try {
      SanityChecker.validatePresenceForQuestionId(context);
      SanityChecker.validatePresenceForRubricId(context);
      SanityChecker.validatePresenceForUserId(context);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    LazyList<AJEntityQuestion> questions = AJEntityQuestion
        .findBySQL(AJEntityQuestion.VALIDATE_EXISTS_NON_DELETED,
            AJEntityQuestion.QUESTION, context.questionId(), false);
    // Question should be present in DB
    if (questions.size() < 1) {
      LOGGER.warn("Question id: {} not present in DB", context.questionId());
      return new ExecutionResult<>(
          MessageResponseFactory
              .createNotFoundResponse(
                  RESOURCE_BUNDLE.getString("question.id") + context.questionId()),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    //Verify if type of question is allowed for rubric association.
    this.question = questions.get(0);
    if (!AJEntityQuestion.RUBRIC_ASSOCIATION_ALLOWED_TYPES
        .contains(this.question.getString(AJEntityQuestion.CONTENT_SUBFORMAT))) {
      LOGGER.warn("Rubric association is not allowed with question type");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(
                  RESOURCE_BUNDLE.getString("rubric.association.not.allowed")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    //Rubric should be present in DB
    LazyList<AJEntityRubric> rubrics =
        AJEntityRubric.findBySQL(AJEntityRubric.VALIDATE_EXISTS_NOT_DELETED, context.rubricId());
    // Rubric should be present in DB
    if (rubrics.size() < 1) {
      LOGGER.warn("Rubric id: {} not present in DB", context.rubricId());
      return new ExecutionResult<>(
          MessageResponseFactory
              .createNotFoundResponse(RESOURCE_BUNDLE.getString("rubric.not.found")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    associatingRubric = rubrics.get(0);

    if (!authorized()) {
      // Association is forbidden
      LOGGER
          .debug("user is not authorized to associate rubric to question:{}", context.questionId());
      return new ExecutionResult<>(
          MessageResponseFactory
              .createForbiddenResponse(
                  RESOURCE_BUNDLE.getString("not.owner.collaborator.on.course.collection")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    try {
      copyRubricIfNeeded();
      markAlreadyAssociatedRubricAsDeleted();
      associateRubricWithQuestion();
      updateQuestionWithAttributesFromRubric();

      LOGGER.info("rubric:{} has been associated with question:{} successfully",
          this.rubric.getId().toString(), context.questionId());
      return new ExecutionResult<>(MessageResponseFactory
          .createNoContentResponse(RESOURCE_BUNDLE.getString("associated"), EventBuilderFactory
              .getAssociateRubricWithQuestionEventBuilder(this.rubric.getId().toString(),
                  this.context.questionId())),
          ExecutionResult.ExecutionStatus.SUCCESSFUL);
    } catch (MessageResponseWrapperException mrwe) {
      return new ExecutionResult<>(mrwe.getMessageResponse(),
          ExecutionResult.ExecutionStatus.FAILED);
    }
  }

  private void updateQuestionWithAttributesFromRubric() {
    question.setMaxScore(rubric.getMaxScore());
    if (!question.save()) {
      LOGGER.debug("Error while copying max score from rubric '{}' to question '{}'",
          this.rubric.getId().toString(), context.questionId());
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()));
    }
  }

  private void associateRubricWithQuestion() {
    this.rubric.setCourseId(this.question.getCourseId());
    this.rubric.setUnitId(this.question.getUnitId());
    this.rubric.setLessonId(this.question.getLessonId());
    this.rubric.setCollectionId(this.question.getCollectionId());
    this.rubric.setContentId(context.questionId());

    if (!this.rubric.save()) {
      LOGGER.debug("error while associating rubric '{}' to question '{}'",
          this.rubric.getId().toString(), context.questionId());
      throw new MessageResponseWrapperException(
          MessageResponseFactory.createValidationErrorResponse(getModelErrors()));
    }
  }

  private void markAlreadyAssociatedRubricAsDeleted() {
    // check if the question has already associated rubric
    // if yes, mark it as deleted.
    LazyList<AJEntityRubric> existingRubrics =
        AJEntityRubric
            .findBySQL(AJEntityRubric.SELECT_EXISTING_RUBRIC_FOR_QUESTION, context.questionId());
    if (existingRubrics.size() >= 1) {
      AJEntityRubric existingRubric = existingRubrics.get(0);
      String existingRubricId = existingRubric.getString(AJEntityRubric.ID);
      //Ignore if the existing rubric and new rubric are same
      if (!existingRubricId.equalsIgnoreCase(context.rubricId())) {
        AJEntityRubric.update(AJEntityRubric.UPDATE_RUBRIC_MARK_DELETED,
            AJEntityRubric.UPDATE_RUBRIC_MARK_DELETED_CONDITION, context.userId(),
            existingRubricId);
        LOGGER.debug("existing rubric '{}' is marked as deleted", existingRubricId);
      }
    }
  }

  private void copyRubricIfNeeded() {
    // If the rubric is ON, create copy. The rule is to
    // associate only copy of the rubric if the rubric is ON
    boolean isRubric = associatingRubric.getBoolean(AJEntityRubric.IS_RUBRIC);
    if (isRubric) {

      copiedRubricId = UUID.randomUUID();

      int count = Base
          .exec(AJEntityRubric.COPY_RUBRIC, copiedRubricId, context.userId(), context.userId(),
              context.rubricId(), context.rubricId(), context.tenant(), context.tenantRoot(),
              context.rubricId());
      if (count == 0) {
        LOGGER.error("error while copying rubric");
        throw new MessageResponseWrapperException(
            MessageResponseFactory.createInternalErrorResponse());
      }
      LOGGER.debug("rubric is ON, created copy '{}'", copiedRubricId.toString());
      this.rubric = AJEntityRubric.findById(copiedRubricId);
    } else {
      //Rubric is OFF, so no copy
      LOGGER.debug("rubric is OFF, No need to create copy");
      this.rubric = associatingRubric;
    }
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
        authRecordCount = Base
            .count(AJEntityQuestion.TABLE_COURSE, AJEntityQuestion.AUTH_FILTER, course,
                context.userId(), context.userId());
        if (authRecordCount >= 1) {
          // Auth check successful
          LOGGER.debug("Auth check successful based on course: {}", course);
          return true;
        }
      } else if (collection != null) {
        // Check if the user is one of collaborator on collection, we do
        // not need to check about course now
        authRecordCount = Base
            .count(AJEntityQuestion.TABLE_COLLECTION, AJEntityQuestion.AUTH_FILTER,
                collection, context.userId(), context.userId());
        if (authRecordCount >= 1) {
          LOGGER.debug("Auth check successful based on collection: {}", collection);
          return true;
        }
      }
    }

    return false;
  }

  private JsonObject getModelErrors() {
    JsonObject errors = new JsonObject();
    this.question.errors().entrySet()
        .forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
    return errors;
  }

}
