package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.QuestionRepo;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
public class AJQuestionRepo implements QuestionRepo {
  private final ProcessorContext context;

  public AJQuestionRepo(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public MessageResponse updateQuestion() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public MessageResponse fetchQuestion() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public MessageResponse createQuestion() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
}
