package org.gooru.nucleus.handlers.questions.processors.commands;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * @author szgooru Created On: 24-Feb-2017
 */
public class RubricCreateProcessor extends AbstractCommandProcessor {

  protected RubricCreateProcessor(ProcessorContext context) {
    super(context);
  }

  @Override
  protected void setDeprecatedVersions() {
    //NOOP
  }

  @Override
  protected MessageResponse processCommand() {
    return RepoBuilder.buildRubricRepo(context).createRubric();
  }

}
