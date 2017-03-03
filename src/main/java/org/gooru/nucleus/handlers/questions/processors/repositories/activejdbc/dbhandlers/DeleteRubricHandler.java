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
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru Created On: 27-Feb-2017
 */
public class DeleteRubricHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(DeleteRubricHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    private final ProcessorContext context;
    private AJEntityRubric rubric;

    public DeleteRubricHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
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
        LazyList<AJEntityRubric> rubrics =
            AJEntityRubric.findBySQL(AJEntityRubric.VALIDATE_EXISTS_NOT_DELETED, context.rubricId());
        // Rubric should be present in DB
        if (rubrics.size() < 1) {
            LOGGER.warn("Rubric id: {} not present in DB", context.rubricId());
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("rubric.not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        rubric = rubrics.get(0);

        if (!authorized()) {
            // Delete is forbidden
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("not.rubric.creator")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if (rubric.getString(AJEntityRubric.ORIGINAL_RUBRIC_ID) != null) {
            LazyList<AJEntityQuestion> questions =
                AJEntityQuestion.findBySQL(AJEntityRubric.SELECT_QUESTION_FOR_RUBRIC, context.rubricId());
            if (!questions.isEmpty()) {
                return new ExecutionResult<>(
                    MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("delete.now.allowed")),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        rubric.setModifierId(context.userId());
        rubric.setBoolean(AJEntityRubric.IS_DELETED, true);

        if (!rubric.save()) {
            LOGGER.warn("error while deleting rubric:{}", context.rubricId());
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(getModelErrors()),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(RESOURCE_BUNDLE.getString("deleted"),
                EventBuilderFactory.getDeleteRubricEventBuilder(this.context.rubricId())),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

    private boolean authorized() {
        String creator = rubric.getString(AJEntityQuestion.CREATOR_ID);
        if (creator != null && creator.equalsIgnoreCase(context.userId())) {
            return true;
        }
        return false;
    }

    private JsonObject getModelErrors() {
        JsonObject errors = new JsonObject();
        this.rubric.errors().entrySet().forEach(entry -> errors.put(entry.getKey(), entry.getValue()));
        return errors;
    }

}
