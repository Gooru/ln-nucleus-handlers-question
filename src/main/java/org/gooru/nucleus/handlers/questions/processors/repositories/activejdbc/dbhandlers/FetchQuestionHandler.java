package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.List;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbauth.AuthorizerBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
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
 * Created by ashish on 11/1/16.
 */
class FetchQuestionHandler implements DBHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchQuestionHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");
    private final ProcessorContext context;
    private AJEntityQuestion question;
    private AJEntityRubric rubric;

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

        LazyList<AJEntityQuestion> questions = AJEntityQuestion
            .findBySQL(AJEntityQuestion.FETCH_QUESTION, AJEntityQuestion.QUESTION, context.questionId(), false);
        // Question should be present in DB
        if (questions.size() < 1) {
            LOGGER.warn("Question id: {} not present in DB", context.questionId());
            return new ExecutionResult<>(MessageResponseFactory
                .createNotFoundResponse(RESOURCE_BUNDLE.getString("question.id") + context.questionId()),
                ExecutionResult.ExecutionStatus.FAILED);
        }
        question = questions.get(0);

        if (AJEntityQuestion.RUBRIC_ASSOCIATION_ALLOWED_TYPES
            .contains(question.getString(AJEntityQuestion.CONTENT_SUBFORMAT))) {
            LazyList<AJEntityRubric> rubrics =
                AJEntityRubric.findBySQL(AJEntityRubric.FETCH_RUBRIC_SUMMARY, context.questionId());

            if (rubrics != null && !rubrics.isEmpty()) {
                this.rubric = rubrics.get(0);
            }
        }
        return AuthorizerBuilder.buildTenantAuthorizer(this.context).authorize(question);

    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        JsonObject response = new JsonObject(JsonFormatterBuilder
            .buildSimpleJsonFormatter(false, AJEntityQuestion.FETCH_QUESTION_FIELDS).toJson(this.question));

        if (this.rubric != null) {
            List<String> RUBRIC_FIELDS;
            if (rubric.getBoolean(AJEntityRubric.IS_RUBRIC)) {
                RUBRIC_FIELDS = AJEntityRubric.RUBRIC_SUMMARY;
            } else {
                RUBRIC_FIELDS = AJEntityRubric.SCORING_FIELDS;
                response.put(AJEntityRubric.MAX_SCORE, this.rubric.getInteger(AJEntityRubric.MAX_SCORE));
            }
            response.put(AJEntityQuestion.RUBRIC, new JsonObject(JsonFormatterBuilder
                .buildSimpleJsonFormatter(false, RUBRIC_FIELDS).toJson(this.rubric)));
        } else {
            response.putNull(AJEntityQuestion.RUBRIC);
        }
        return new ExecutionResult<>(MessageResponseFactory.createOkayResponse(response),
            ExecutionResult.ExecutionStatus.SUCCESSFUL);
    }

    @Override
    public boolean handlerReadOnly() {
        return true;
    }
}
