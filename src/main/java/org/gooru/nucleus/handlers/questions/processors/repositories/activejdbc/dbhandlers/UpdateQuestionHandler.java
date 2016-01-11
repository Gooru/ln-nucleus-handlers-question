package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * Created by ashish on 11/1/16.
 */
class UpdateQuestionHandler implements DBHandler {
  private final ProcessorContext context;

  public UpdateQuestionHandler(ProcessorContext context) {
    this.context = context;
  }

  @Override
  public ExecutionResult<MessageResponse> checkSanity() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");

  }

  @Override
  public ExecutionResult<MessageResponse> validateRequest() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public ExecutionResult<MessageResponse> executeRequest() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }

  @Override
  public boolean handlerReadOnly() {
    // TODO: Provide a concrete implementation
    throw new IllegalStateException("Not implemented yet");
  }
}
