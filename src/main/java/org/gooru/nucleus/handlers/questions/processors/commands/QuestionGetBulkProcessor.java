package org.gooru.nucleus.handlers.questions.processors.commands;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * @author szgooru
 * Created On: 09-Aug-2017
 */
public class QuestionGetBulkProcessor extends AbstractCommandProcessor {

    protected QuestionGetBulkProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        // NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        return RepoBuilder.buildQuestionRepo(context).fetchBulkQuestions();
    }

}
