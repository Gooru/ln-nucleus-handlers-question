package org.gooru.nucleus.handlers.questions.processors.repositories;

import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public interface QuestionRepo {
  MessageResponse updateQuestion();

  MessageResponse fetchQuestion();

  MessageResponse createQuestion();
}
