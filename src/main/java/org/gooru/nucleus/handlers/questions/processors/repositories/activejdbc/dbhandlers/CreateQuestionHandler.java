package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbutils.LicenseUtil;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ashish on 11/1/16.
 */
class CreateQuestionHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateQuestionHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private final ProcessorContext context;
  private AJEntityQuestion question;

  CreateQuestionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // There should be a question id present
    if ((context.request() == null) || context.request().isEmpty()) {
      LOGGER.warn("Invalid request payload");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    if ((context.userId() == null) || context.userId().isEmpty() || context.userId()
        .equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      return new ExecutionResult<>(
          MessageResponseFactory
              .createForbiddenResponse(RESOURCE_BUNDLE.getString("anonymous.user")),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    JsonObject errors = validateForbiddenFields();
    if ((errors != null) && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }
    // Our validators should certify this
    errors = new DefaultPayloadValidator()
        .validatePayload(context.request(), AJEntityQuestion.createFieldSelector(),
            AJEntityQuestion.getValidatorRegistry());
    if ((errors != null) && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    question = new AJEntityQuestion();

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
      LOGGER.error("Question creation failed for user '{}'", context.userId());
      if (question.hasErrors()) {
        Map<String, String> map = question.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }

    return new ExecutionResult<>(
        MessageResponseFactory.createCreatedResponse(question.getId().toString(),
            EventBuilderFactory.getCreateQuestionEventBuilder(question.getId().toString())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject validateForbiddenFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    AJEntityQuestion.INSERT_QUESTION_FORBIDDEN_FIELDS.stream()
        .filter(invalidField -> input.getValue(invalidField) != null)
        .forEach(invalidField -> output.put(invalidField, "Field not allowed"));
    return output.isEmpty() ? null : output;
  }

  private void autoPopulate() {
    question.setModifierId(context.userId());
    question.setCreatorId(context.userId());
    question.setContentFormatQuestion();
    question.setLicense(LicenseUtil.getDefaultLicenseCode());
    question.setTenant(context.tenant());
    question.setDefaultMaxScore();
    String tenantRoot = context.tenantRoot();
    if (tenantRoot != null && !tenantRoot.isEmpty()) {
      question.setTenantRoot(tenantRoot);
    }
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityQuestionEntityBuilder implements
      EntityBuilder<AJEntityQuestion> {

  }

}
