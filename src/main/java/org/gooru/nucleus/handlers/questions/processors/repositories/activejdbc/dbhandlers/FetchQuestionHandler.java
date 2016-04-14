package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.formatter.JsonFormatterBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.javalite.activejdbc.LazyList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.vertx.core.json.JsonObject;

/**
 * Created by ashish on 11/1/16.
 */
class FetchQuestionHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchQuestionHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityQuestion question;

    FetchQuestionHandler(ProcessorContext context) {
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
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {

        LazyList<AJEntityQuestion> questions = AJEntityQuestion.findBySQL(AJEntityQuestion.FETCH_QUESTION,
            AJEntityQuestion.QUESTION, context.questionId(), false);
        // Question should be present in DB
        if (questions.size() < 1) {
            LOGGER.warn("Question id: {} not present in DB", context.questionId());
            return new ExecutionResult<>(
                MessageResponseFactory
                    .createNotFoundResponse(RESOURCE_BUNDLE.getString("question.id") + context.questionId()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        question = questions.get(0);
        return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        return new ExecutionResult<>(
            MessageResponseFactory.createOkayResponse(new JsonObject(JsonFormatterBuilder
                .buildSimpleJsonFormatter(false, AJEntityQuestion.FETCH_QUESTION_FIELDS).toJson(this.question))),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }
}
