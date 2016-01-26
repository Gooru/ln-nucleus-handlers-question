package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;

/**
 * Created by ashish on 11/1/16.
 */
public class DBHandlerBuilder {
  public DBHandler buildUpdateQuestionHandler(ProcessorContext context) {
    return new UpdateQuestionHandler(context);

  }

  public DBHandler buildFetchQuestionHandler(ProcessorContext context) {
    return new FetchQuestionHandler(context);
  }

  public DBHandler buildCreateQuestionHandler(ProcessorContext context) {
    return new CreateQuestionHandler(context);

  }

  public DBHandler buildDeleteQuestionHandler(ProcessorContext context) {
    return new DeleteQuestionHandler(context);
  }
}
