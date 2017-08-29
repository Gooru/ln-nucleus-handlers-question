package org.gooru.nucleus.handlers.questions.processors.utils;

import java.util.UUID;

import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 29/12/16.
 */
public final class ValidationUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(ValidationUtils.class);

    private ValidationUtils() {
        throw new AssertionError();
    }

    public static boolean validateUser(String userId) {
        return !(userId == null || userId.isEmpty()) && (userId.equalsIgnoreCase(MessageConstants.MSG_USER_ANONYMOUS)
            || validateUuid(userId));
    }

    public static boolean validateId(String id) {
        return !(id == null || id.isEmpty()) && validateUuid(id);
    }

    public static boolean isIdInvalid(ProcessorContext context) {
        if (context.questionId() == null || context.questionId().isEmpty()) {
            LOGGER.error("Invalid request, question id not available. Aborting");
            return true;
        }
        return !validateUuid(context.questionId());
    }
    
    public static boolean isValidRubricId(ProcessorContext context) {
        if (context.rubricId() == null || context.rubricId().isEmpty()) {
            LOGGER.error("Invalid request, rubric id not available. Aborting");
            return true;
        }
        return !validateUuid(context.rubricId());
    }

    private static boolean validateUuid(String uuidString) {
        try {
            UUID.fromString(uuidString);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        } catch (Exception e) {
            return false;
        }
    }

}
