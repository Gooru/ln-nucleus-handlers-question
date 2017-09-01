package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.Base;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 02-Sep-2017
 */
public class UpdateQuestionScoreHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateQuestionHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityQuestion question;
    private AJEntityRubric rubric;

    public UpdateQuestionScoreHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        // There should be a question id present
        if (context.questionId() == null || context.questionId().isEmpty()) {
            LOGGER.warn("Missing question id");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.question.id")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // The user should not be anonymous
        if (context.userId() == null || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("anonymous.user")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // Payload should not be empty
        if (context.request() == null || context.request().isEmpty()) {
            LOGGER.warn("Empty payload supplied to edit question");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("payload.empty")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // Our validators should certify this
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
            AJEntityRubric.scoringFieldSelector(), AJEntityRubric.getValidatorRegistry());
        if (errors != null && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
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

        this.question = questions.get(0);
        if (!authorized()) {
            // Update is forbidden
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.owner.collaborator.on.course.collection")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // Verify if type of question is allowed for scoring.
        this.question = questions.get(0);
        if (!AJEntityQuestion.RUBRIC_ASSOCIATION_ALLOWED_TYPES
            .contains(this.question.getString(AJEntityQuestion.CONTENT_SUBFORMAT))) {
            LOGGER.warn("Scoring is not allowed with question type");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("scoring.not.allowed")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        boolean scoring = context.request().getBoolean(AJEntityRubric.SCORING);
        LOGGER.debug("updating scoring of question '{}' to '{}'", context.questionId(), scoring);

        // If scoring is false which mean user is trying to set scoring OFF for
        // the question. In this case, we will delete the dummy rubric created
        // to store the scoring of the question.
        if (!scoring) {
            // Check for the rubric record by question id
            LazyList<AJEntityRubric> rubrics =
                AJEntityRubric.findBySQL(AJEntityRubric.SELECT_EXISTING_RUBRIC_FOR_QUESTION, context.questionId());
            if (rubrics.size() >= 1) {
                if (!rubrics.get(0).delete()) {
                    return new ExecutionResult<>(
                        MessageResponseFactory
                            .createInternalErrorResponse(RESOURCE_BUNDLE.getString("unable.to.update.scoring")),
                        ExecutionResult.ExecutionStatus.FAILED);
                }

                LOGGER.debug("dummy rubric deleted successfully");
                return new ExecutionResult<>(
                    MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated")),
                    ExecutionResult.ExecutionStatus.SUCCESSFUL);
            }

            // Silently ignore and return success if there is no record for
            // rubric by question id
            return new ExecutionResult<>(
                MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated")),
                ExecutionResult.ExecutionStatus.SUCCESSFUL);
        }

        // If already rubric record for the question when use for updating
        // Otherwise create new dummy rubric to store scoring of the question
        LazyList<AJEntityRubric> existingRubrics =
            AJEntityRubric.findBySQL(AJEntityRubric.SELECT_EXISTING_RUBRIC_FOR_QUESTION, context.questionId());
        if (existingRubrics.size() >= 1) {
            this.rubric = existingRubrics.get(0);
        } else {
            this.rubric = new AJEntityRubric();
            autoPopulate();
        }

        new DefaultAJEntityRubricEntityBuilder().build(rubric, context.request(),
            AJEntityRubric.getConverterRegistry());
        boolean result = rubric.save();
        if (!result) {
            LOGGER.error("Question score store (Rubric creation) failed for question '{}'", context.questionId());
            if (rubric.hasErrors()) {
                Map<String, String> map = rubric.errors();
                JsonObject errors = new JsonObject();
                map.forEach(errors::put);
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        LOGGER.debug("question score stored (rubric created/udpated) successfully");
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("updated")),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private void autoPopulate() {
        rubric.setModifierId(context.userId());
        rubric.setCreatorId(context.userId());
        rubric.setTenant(context.tenant());
        String tenantRoot = context.tenantRoot();
        if (tenantRoot != null && !tenantRoot.isEmpty()) {
            rubric.setTenantRoot(tenantRoot);
        }
        rubric.setBoolean(AJEntityRubric.IS_RUBRIC, false);
        rubric.setContentId(context.questionId());
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

    private static class DefaultPayloadValidator implements PayloadValidator {
    }

    private static class DefaultAJEntityRubricEntityBuilder implements EntityBuilder<AJEntityRubric> {
    }
}
