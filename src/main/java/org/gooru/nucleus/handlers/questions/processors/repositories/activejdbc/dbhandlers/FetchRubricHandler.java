package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers;

import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.responses.ExecutionResult;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author szgooru
 * Created On: 27-Feb-2017
 */
public class FetchRubricHandler implements DBHandler {
    
    private static final Logger LOGGER = LoggerFactory.getLogger(FetchRubricHandler.class);
    private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    private final ProcessorContext context;

    public FetchRubricHandler(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public ExecutionResult<MessageResponse> checkSanity() {
        return null;
    }

    @Override
    public ExecutionResult<MessageResponse> validateRequest() {
        return null;
    }

    @Override
    public ExecutionResult<MessageResponse> executeRequest() {
        return null;
    }

    @Override
    public boolean handlerReadOnly() {
        return false;
    }

}
