package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;

/**
 * Created by ashish on 11/1/16.
 */
public final class DBHandlerBuilder {

  private DBHandlerBuilder() {
    throw new AssertionError();
  }

  public static DBHandler buildUpdateQuestionHandler(ProcessorContext context) {
    return new UpdateQuestionHandler(context);

  }

  public static DBHandler buildFetchQuestionHandler(ProcessorContext context) {
    return new FetchQuestionHandler(context);
  }

  public static DBHandler buildCreateQuestionHandler(ProcessorContext context) {
    return new CreateQuestionHandler(context);
  }

  public static DBHandler buildDeleteQuestionHandler(ProcessorContext context) {
    return new DeleteQuestionHandler(context);
  }

  public static DBHandler buildUpdateRubricHandler(ProcessorContext context) {
    return new UpdateRubricHandler(context);
  }

  public static DBHandler buildFetchRubricHandler(ProcessorContext context) {
    return new FetchRubricHandler(context);
  }

  public static DBHandler buildCreateRubricHandler(ProcessorContext context) {
    return new CreateRubricHandler(context);
  }

  public static DBHandler buildDeleteRubricHandler(ProcessorContext context) {
    return new DeleteRubricHandler(context);
  }

  public static DBHandler buildAssociateRubricWithQuestionHandler(ProcessorContext context) {
    return new AssociateRubricWithQuestionHandler(context);
  }

  public static DBHandler buildFetchBulkQuestionsHandler(ProcessorContext context) {
    return new FetchBulkQuestionsHandler(context);
  }

  public static DBHandler buildUpdateQuestionScoreHandler(ProcessorContext context) {
    return new UpdateQuestionScoreHandler(context);
  }
}
