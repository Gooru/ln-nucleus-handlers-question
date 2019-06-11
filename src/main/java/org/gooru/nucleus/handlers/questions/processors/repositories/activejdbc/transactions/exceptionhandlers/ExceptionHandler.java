package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.transactions.exceptionhandlers;

import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

public interface ExceptionHandler {

  ExecutionResult<MessageResponse> handleError(Throwable e);

}
