package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.events.EventBuilderFactory;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers.helpers.TaxonomyHelper;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityRubric;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entitybuilders.EntityBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.FieldSelector;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators.PayloadValidator;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.gooru.nucleus.handlers.questions.processors.utils.HelperUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru Created On: 27-Feb-2017
 */
public class CreateRubricHandler implements DBHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CreateRubricHandler.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private final ProcessorContext context;
  private AJEntityRubric rubric;

  public CreateRubricHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    if ((context.request() == null) || context.request().isEmpty()) {
      LOGGER.warn("Invalid request payload");
      return new ExecutionResult<>(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("empty.payload")),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    if ((context.userId() == null) || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
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
    Boolean isRubric = context.request().getBoolean(AJEntityRubric.IS_RUBRIC);

    if (isRubric == null) {
      errors = new JsonObject();
      errors.put(AJEntityRubric.IS_RUBRIC, RESOURCE_BUNDLE.getString("missing.mandatory.field"));
      LOGGER.warn("is_rubric field is missing");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    LOGGER.debug("creating rubric with is_rubric={}", isRubric);
    FieldSelector fs;
    if (isRubric) {
      fs = AJEntityRubric.createRubricOnFieldSelector();
    } else {
      fs = AJEntityRubric.createRubricOffFieldSelector();
    }

    errors =
        new DefaultPayloadValidator()
            .validatePayload(context.request(), fs, AJEntityRubric.getValidatorRegistry());
    if ((errors != null) && !errors.isEmpty()) {
      LOGGER.warn("Validation errors for request");
      return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
          ExecutionResult.ExecutionStatus.FAILED);
    }

    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    rubric = new AJEntityRubric();
    return new ExecutionResult<>(null, ExecutionResult.ExecutionStatus.CONTINUE_PROCESSING);
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    autoPopulate();
    new DefaultAJEntityRubricEntityBuilder().build(rubric, context.request(),
        AJEntityRubric.getConverterRegistry());
    boolean result = rubric.save();
    if (!result) {
      LOGGER.error("Rubric creation failed for user '{}'", context.userId());
      if (rubric.hasErrors()) {
        Map<String, String> map = rubric.errors();
        JsonObject errors = new JsonObject();
        map.forEach(errors::put);
        return new ExecutionResult<>(MessageResponseFactory.createValidationErrorResponse(errors),
            ExecutionResult.ExecutionStatus.FAILED);
      }
    }

    LOGGER.debug("rubric created successfully");
    return new ExecutionResult<>(
        MessageResponseFactory.createCreatedResponse(rubric.getId().toString(),
            EventBuilderFactory.getCreateRubricEventBuilder(rubric.getId().toString())),
        ExecutionResult.ExecutionStatus.SUCCESSFUL);
  }

  @Override
  public boolean handlerReadOnly() {
    return false;
  }

  private JsonObject validateForbiddenFields() {
    JsonObject input = context.request();
    JsonObject output = new JsonObject();
    AJEntityRubric.INSERT_RUBRIC_FORBIDDEN_FIELDS.stream()
        .filter(invalidField -> input.getValue(invalidField) != null)
        .forEach(invalidField -> output
            .put(invalidField, RESOURCE_BUNDLE.getString("field.not.allowed")));
    return output.isEmpty() ? null : output;
  }

  private void autoPopulate() {
    rubric.setModifierId(context.userId());
    rubric.setCreatorId(context.userId());
    rubric.setTenant(context.tenant());
    String tenantRoot = context.tenantRoot();
    if (tenantRoot != null && !tenantRoot.isEmpty()) {
      rubric.setTenantRoot(tenantRoot);
    }

    JsonArray gutCodes = TaxonomyHelper.populateGutCodes(context.request());
    if (gutCodes != null) {
      String strGC = HelperUtils.toPostgresTextArrayFromJsonArray(gutCodes);
      rubric.setGutCodes(strGC);
    }
  }

  private static class DefaultPayloadValidator implements PayloadValidator {

  }

  private static class DefaultAJEntityRubricEntityBuilder implements EntityBuilder<AJEntityRubric> {

  }

}
