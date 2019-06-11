package org.gooru.nucleus.handlers.questions.processors.commands;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * @author ashish on 2/1/17.
 */
class QuestionCreateProcessor extends AbstractCommandProcessor {

  public QuestionCreateProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {

  }

  @Override
  protected MessageResponse processCommand() {
    return RepoBuilder.buildQuestionRepo(context).createQuestion();
  }
}
