package org.gooru.nucleus.handlers.questions.processors.commands;

import static org.gooru.nucleus.handlers.questions.processors.utils.ValidationUtils.isIdInvalid;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;

/**
 * @author ashish on 2/1/17.
 */
class QuestionGetProcessor extends AbstractCommandProcessor {
    public QuestionGetProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {

    }

    @Override
    protected MessageResponse processCommand() {
        if (isIdInvalid(context)) {
            return MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.question.id"));
        }
        return RepoBuilder.buildQuestionRepo(context).fetchQuestion();
    }
}
