package org.gooru.nucleus.handlers.questions.processors.commands;

import static org.gooru.nucleus.handlers.questions.processors.utils.ValidationUtils.isValidRubricId;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RepoBuilder;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;

/**
 * @author szgooru
 * Created On: 24-Feb-2017
 */
public class RubricGetProcessor extends AbstractCommandProcessor {

    protected RubricGetProcessor(ProcessorContext context) {
        super(context);
    }

    @Override
    protected void setDeprecatedVersions() {
        //NOOP
    }

    @Override
    protected MessageResponse processCommand() {
        if (isValidRubricId(context)) {
            return MessageResponseFactory
                .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.rubric.id"));
        }
        return RepoBuilder.buildRubricRepo(context).fetchRubric();
    }

}
