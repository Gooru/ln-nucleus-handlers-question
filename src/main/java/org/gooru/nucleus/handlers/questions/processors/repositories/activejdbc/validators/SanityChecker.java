package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.validators;

import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.exceptions.MessageResponseWrapperException;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish.
 */

public final class SanityChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(SanityChecker.class);
  private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

  private SanityChecker() {
    throw new AssertionError();
  }

  public static void validatePresenceForQuestionId(ProcessorContext context) {
    if (context.questionId() == null || context.questionId().isEmpty()) {
      LOGGER.warn("Missing question id");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.question.id")));
    }
  }

  public static void validatePresenceForRubricId(ProcessorContext context) {
    if (context.rubricId() == null || context.rubricId().isEmpty()) {
      LOGGER.warn("Missing rubric id");
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("missing.rubric.id")));
    }

  }

  public static void validatePresenceForUserId(ProcessorContext context) {
    if ((context.userId() == null) || context.userId().isEmpty()
        || context.userId().equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)) {
      throw new MessageResponseWrapperException(
          MessageResponseFactory
              .createForbiddenResponse(RESOURCE_BUNDLE.getString("anonymous.user")));
    }

  }

}
