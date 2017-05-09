package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 27-Feb-2017
 */
public class AssociateRubricWithQuestionHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(AssociateRubricWithQuestionHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    private final ProcessorContext context;
    private AJEntityQuestion question;
    private AJEntityRubric rubric;

    public AssociateRubricWithQuestionHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {

        if (context.questionId() == null || context.questionId().isEmpty()) {
            LOGGER.warn("Missing question id");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.question.id")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (context.rubricId() == null || context.rubricId().isEmpty()) {
            LOGGER.warn("Missing rubric id");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.rubric.id")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if ((context.userId() == null) || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("anonymous.user")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityQuestion> questions = AJEntityQuestion.findBySQL(AJEntityQuestion.VALIDATE_EXISTS_NON_DELETED,
            AJEntityQuestion.QUESTION, context.questionId(), false);
        // Question should be present in DB
        if (questions.size() < 1) {
            LOGGER.warn("Question id: {} not present in DB", context.questionId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createNotFoundResponse(RESOURCE_BUNDLE.getString("question.id") + context.questionId()),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        //Verify if type of question is allowed for rubric association.
        this.question = questions.get(0);
        if (!AJEntityQuestion.RUBRIC_ASSOCIATION_ALLOWED_TYPES
            .contains(this.question.getString(AJEntityQuestion.CONTENT_SUBFORMAT))) {
            LOGGER.warn("Rubric association is not allowed with question type");
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("rubric.association.not.allowed")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        //Rubric shuold be present in DB
        LazyList<AJEntityRubric> rubrics =
            AJEntityRubric.findBySQL(AJEntityRubric.VALIDATE_EXISTS_NOT_DELETED, context.rubricId());
        // Rubric should be present in DB
        if (rubrics.size() < 1) {
            LOGGER.warn("Rubric id: {} not present in DB", context.rubricId());
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("rubric.not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        this.rubric = rubrics.get(0);
        
        // If the rubric is ON it should NOT be original rubric. The rule is to
        // associate only copy of the rubric if the rubric is ON
        boolean isRubric = this.rubric.getBoolean(AJEntityRubric.IS_RUBRIC);
        if (isRubric) {
            if (this.rubric.getString(AJEntityRubric.ORIGINAL_RUBRIC_ID) == null) {
                LOGGER.warn("original rubric cannot be associated");
                return new ExecutionResult<>(
                    MessageResponseFactory
                        .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("orig.rubric.association.not.allowed")),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }
        
        if (!authorized()) {
            // Association is forbidden
            LOGGER.debug("user is not authorized to associate rubric to question:{}", context.questionId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.owner.collaborator.on.course.collection")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        
        // check if the question has already associated rubric
        // if yes, mark it as deleted.
        LazyList<AJEntityRubric> existingRubrics =
            AJEntityRubric.findBySQL(AJEntityRubric.SELECT_EXISTING_RUBRIC_FOR_QUESTION, context.questionId());
        if (existingRubrics.size() >= 1) {
            AJEntityRubric existingRubric = existingRubrics.get(0);
            String existingRubricId = existingRubric.getString(AJEntityRubric.ID);
            //Ignore if the existing rubric and new rubric are same
            if(!existingRubricId.equalsIgnoreCase(context.rubricId())) {
                AJEntityRubric.update(AJEntityRubric.UPDATE_RUBRIC_MARK_DELETED,
                    AJEntityRubric.UPDATE_RUBRIC_MARK_DELETED_CONDITION, context.userId(), existingRubricId);
                LOGGER.debug("existing rubric '{}' is marked as deleted", existingRubricId);
            }
        }
        
        this.rubric.setCourseId(this.question.getCourseId());
        this.rubric.setUnitId(this.question.getUnitId());
        this.rubric.setLessonId(this.question.getLessonId());
        this.rubric.setCollectionId(this.question.getCollectionId());
        this.rubric.setContentId(context.questionId());
        
        if (!this.rubric.save()) {
            LOGGER.debug("error while associating rubric '{}' to question '{}'", context.rubricId(),
                context.questionId());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        LOGGER.info("rubric:{} has been associated with question:{} successfully", context.rubricId(),
            context.questionId());
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("associated"), EventBuilderFactory
                .getAssociateRubricWithQuestionEventBuilder(this.context.rubricId(), this.context.questionId())),
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
                authRecordCount = Base.count(AJEntityQuestion.TABLE_COURSE, AJEntityQuestion.AUTH_FILTER, course,
                    context.userId(), context.userId());
                if (authRecordCount >= 1) {
                    // Auth check successful
                    LOGGER.debug("Auth check successful based on course: {}", course);
                    return true;
                }
            } else if (collection != null) {
                // Check if the user is one of collaborator on collection, we do
                // not need to check about course now
                authRecordCount = Base.count(AJEntityQuestion.TABLE_COLLECTION, AJEntityQuestion.AUTH_FILTER,
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
        this.question.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }

}
