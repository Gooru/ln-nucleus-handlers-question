package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.Arrays;
import java.util.List;
import java.util.ResourceBundle;
import java.util.UUID;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
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
 * @author szgooru Created On: 09-Aug-2017
 */
public class FetchBulkQuestionsHandler implements DBHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(FetchQuestionHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;

    private List<String> questionIdList;
    private LazyList<AJEntityQuestion> questions;

    public FetchBulkQuestionsHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        JsonArray inputArray = context.request().getJsonArray("ids");
        String questionIds = inputArray != null ? inputArray.getString(0) : null;
        if (questionIds == null || questionIds.isEmpty()) {
            LOGGER.warn("Missing question ids");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.question.id")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        questionIdList = Arrays.asList(questionIds.split(","));
        if (questionIdList.size() > 50) {
            LOGGER.warn("question id size exceeds max limit of 50");
            return new ExecutionResult<>(
                MessageResponseFactory.createInvalidRequestResponse(RESOURCE_BUNDLE.getString("max.question.ids")),
                ExecutionResult.ExecutionStatus.FAILED);
        }

        for (String questionId : questionIdList) {
            try {
                UUID.fromString(questionId);
            } catch (IllegalArgumentException e) {
                LOGGER.warn("invalid format of question id passed in request");
                return new ExecutionResult<>(MessageResponseFactory.createInvalidRequestResponse(
                    RESOURCE_BUNDLE.getString("invalid.question.id")), ExecutionResult.ExecutionStatus.FAILED);
            }
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        this.questions = AJEntityQuestion.findBySQL(AJEntityQuestion.FETCH_QUESTIONS_BULK, AJEntityQuestion.QUESTION,
            HelperUtils.toPostgresArrayString(questionIdList), false);
        if (this.questions.isEmpty()) {
            LOGGER.warn("no question found matching to input");
            return new ExecutionResult<>(
                MessageResponseFactory.createNotFoundResponse(RESOURCE_BUNDLE.getString("not.found")),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        
        // Iterate through all questions and check for tenant authorization
        for (AJEntityQuestion question : this.questions) {
            ExecutionResult<MessageResponse> result =
                AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(question);
            if (result.hasFailed()) {
                return result;
            }
        }

        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonArray questionArray = new JsonArray(JsonFormatterBuilder
            .buildSimpleJsonFormatter(false, AJEntityQuestion.FETCH_QUESTION_FIELDS).toJson(questions));
        JsonObject response = new JsonObject();
        response.put(AJEntityQuestion.RESP_JSON_KEY_QUESTIONS, questionArray);
        return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(response),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }

}
