package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.QuestionRepo;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.transactions.TransactionExecutor;
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
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateQuestionHandler(context));

  }

  @Override
  public MessageResponse fetchQuestion() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildFetchQuestionHandler(context));

  }

  @Override
  public MessageResponse createQuestion() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildCreateQuestionHandler(context));

  }

  @Override
  public MessageResponse deleteQuestion() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildDeleteQuestionHandler(context));
  }

  @Override
  public MessageResponse associateRubricWithQuestion() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildAssociateRubricWithQuestionHandler(context));
  }

  @Override
  public MessageResponse fetchBulkQuestions() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildFetchBulkQuestionsHandler(context));
  }

  @Override
  public MessageResponse updateQuestionScore() {
    return TransactionExecutor
        .executeTransaction(DBHandlerBuilder.buildUpdateQuestionScoreHandler(context));
  }
}
