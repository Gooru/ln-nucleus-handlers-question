package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.QuestionRepo;

/**
 * Created by ashish on 11/1/16.
 */
public class AJQuestionRepoBuilder {
  public QuestionRepo buildQuestionRepo(ProcessorContext context) {
    return new AJQuestionRepo(context);
  }
}
