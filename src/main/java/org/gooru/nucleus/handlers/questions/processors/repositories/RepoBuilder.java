package org.gooru.nucleus.handlers.questions.processors.repositories;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.AJQuestionRepoBuilder;

/**
 * Created by ashish on 11/1/16.
 */
public final class RepoBuilder {

  private RepoBuilder() {
    throw new AssertionError();
  }

  public static QuestionRepo buildQuestionRepo(ProcessorContext context) {
    return AJQuestionRepoBuilder.buildQuestionRepo(context);
  }
}
