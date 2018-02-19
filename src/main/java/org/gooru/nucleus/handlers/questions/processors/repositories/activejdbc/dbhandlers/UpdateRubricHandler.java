package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.Map;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers.helpers.TaxonomyHelper;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.questions.processors.utils.HelperUtils;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 * Created On: 27-Feb-2017
 */
public class UpdateRubricHandler implements DBHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateRubricHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    private final ProcessorContext context;
    private AJEntityRubric rubric;

    public UpdateRubricHandler(ProcessorContext context) {
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
        
        if ((context.request() == null) || context.request().isEmpty()) {
            LOGGER.warn("Invalid request payload");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        if ((context.userId() == null) || context.userId().isEmpty()
            || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
            return new ExecutionResult<>(
                MessageResponseFactory.createForbiddenResponse(RESOURCE_BUNDLE.getString("anonymous.user")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        // Our validators should certify this
        JsonObject errors = new DefaultPayloadValidator().validatePayload(context.request(),
            AJEntityRubric.updateFieldSelector(), AJEntityRubric.getValidatorRegistry());
        if ((errors != null) && !errors.isEmpty()) {
            LOGGER.warn("Validation errors for request");
            return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityRubric> rubrics = AJEntityRubric
            .findBySQL(AJEntityRubric.VALIDATE_EXISTS_NOT_DELETED, context.rubricId());
        // Rubric should be present in DB
        if (rubrics.size() < 1) {
            LOGGER.warn("Rubric id: {} not present in DB", context.rubricId());
            return new ExecutionResult<>(MessageResponseFactory
                .createNotFoundResponse(RESOURCE_BUNDLE.getString("rubric.not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        rubric = rubrics.get(0);
        
        boolean isRemote = context.request().containsKey(AJEntityRubric.IS_REMOTE)
            ? context.request().getBoolean(AJEntityRubric.IS_REMOTE) : false;
        String url = context.request().getString(AJEntityRubric.URL);
        
        if (isRemote && url != null && !url.isEmpty()) {
            boolean isUrlUpdated = checkIfURLUpdated(url, rubric);
            if (isUrlUpdated) {
                JsonArray duplicates = checkDuplicate();
                if (duplicates != null && !duplicates.isEmpty()) {
                    JsonObject response = new JsonObject().put(AJEntityRubric.DUPLICATE_IDS, duplicates);
                    LOGGER.warn("Duplicate rubrics found:{}", duplicates.toString());
                    return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(response),
                        ExecutionResult.ExecutionStatus.FAILED);
                }
            }
        }
        
        if (!authorized()) {
            // Update is forbidden
            return new ExecutionResult<>(MessageResponseFactory
                .createForbiddenResponse(RESOURCE_BUNDLE.getString("not.rubric.creator")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        autoPopulate();
        
        new DefaultAJEntityRubricEntityBuilder().build(rubric, context.request(),
            AJEntityRubric.getConverterRegistry());
        boolean result = rubric.save();
        if (!result) {
            LOGGER.error("Rubric update failed for user '{}'", context.userId());
            if (rubric.hasErrors()) {
                Map<String, String> map = rubric.errors();
                JsonObject errors = new JsonObject();
                map.forEach(errors::put);
                return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
                    ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        LOGGER.debug("rubric updated successfully");
        return new ExecutionResult<>(
            MessageResponseFactory.createNoContentResponse(rubric.getId().toString(),
                EventBuilderFactory.getUpdateRubricEventBuilder(rubric.getId().toString())),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }
    
    private void autoPopulate() {
        rubric.setModifierId(context.userId());
        
        JsonArray gutCodes = TaxonomyHelper.populateGutCodes(context.request());
        if (gutCodes != null) {
            String strGC = HelperUtils.toPostgresTextArrayFromJsonArray(gutCodes);
            rubric.setGutCodes(strGC);
        }
    }
    
    private JsonArray checkDuplicate() {
        JsonArray duplicateArray = new JsonArray();
        LazyList<AJEntityRubric> duplicateRubrics = AJEntityRubric.findBySQL(AJEntityRubric.SELECT_DUPLICATE,
            context.request().getString(AJEntityRubric.URL).toLowerCase(), context.tenant());
        if (!duplicateRubrics.isEmpty()) {
            duplicateRubrics.forEach(dupRubric -> {
                duplicateArray.add(dupRubric.getId().toString());
            });
        }
        return duplicateArray;
    }
    
    private boolean checkIfURLUpdated(String url, AJEntityRubric rubric) {
        String existingURL = rubric.getString(AJEntityRubric.URL);
        return !(existingURL != null && !existingURL.isEmpty() && existingURL.equalsIgnoreCase(url));
    }
    
    private boolean authorized() {
        String creator = rubric.getString(AJEntityQuestion.CREATOR_ID);
        if (creator != null && creator.equalsIgnoreCase(context.userId())) {
            return true;
        }
        return false;
    }
    
    private static class DefaultPayloadValidator implements PayloadValidator {
    }
    
    private static class DefaultAJEntityRubricEntityBuilder implements EntityBuilder<AJEntityRubric> {
    }
}
