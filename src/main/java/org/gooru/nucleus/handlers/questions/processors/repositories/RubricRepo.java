package org.gooru.nucleus.handlers.questions.processors.repositories;

import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * @author szgooru Created On: 24-Feb-2017
 */
public interface RubricRepo {

  MessageResponse updateRubric();

  MessageResponse fetchRubric();

  MessageResponse createRubric();

  MessageResponse deleteRubric();
}
