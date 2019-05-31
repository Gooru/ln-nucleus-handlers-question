package org.gooru.nucleus.handlers.questions.processors.repositories;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.AJQuestionRepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.AJRubricRepoBuilder;

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

  public static RubricRepo buildRubricRepo(ProcessorContext context) {
    return AJRubricRepoBuilder.buildRubricRepo(context);
  }
}
