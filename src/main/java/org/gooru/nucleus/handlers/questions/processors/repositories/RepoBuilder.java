package org.gooru.nucleus.handlers.questions.processors.repositories;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.AJQuestionRepoBuilder;

/**
 * Created by ashish on 11/1/16.
 */
public class RepoBuilder {
  public QuestionRepo buildQuestionRepo(ProcessorContext context) {
    return new AJQuestionRepoBuilder().buildQuestionRepo(context);
  }
}
