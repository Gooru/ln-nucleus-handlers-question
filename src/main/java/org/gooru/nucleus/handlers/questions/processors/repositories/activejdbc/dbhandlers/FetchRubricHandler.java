package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * @author szgooru
 * Created On: 27-Feb-2017
 */
public class FetchRubricHandler implements DBHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchRubricHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    private final ProcessorContext context;
    private AJEntityRubric rubric;

    public FetchRubricHandler(ProcessorContext context) {
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
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        LazyList<AJEntityRubric> rubrics = AJEntityRubric
            .findBySQL(AJEntityRubric.FETCH_RUBRIC, context.rubricId());
        // Rubric should be present in DB
        if (rubrics.size() < 1) {
            LOGGER.warn("Rubric id: {} not present in DB", context.rubricId());
            return new ExecutionResult<>(MessageResponseFactory
                .createNotFoundResponse(RESOURCE_BUNDLE.getString("rubric.not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        rubric = rubrics.get(0);
        //TODO: Do we need to authorize?
        //return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(rubric);
        
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(new JsonObject(
            JsonFormatterBuilder.buildSimpleJsonFormatter(false, AJEntityRubric.FETCH_RUBRIC_FIELDS)
                .toJson(this.rubric))), ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
