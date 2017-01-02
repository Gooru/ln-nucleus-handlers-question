package org.gooru.nucleus.handlers.questions.processors.commands;

import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.gooru.nucleus.handlers.questions.processors.Processor;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;
import org.gooru.nucleus.handlers.questions.processors.utils.VersionValidationUtils;

/**
 * @author ashish on 2/1/17.
 */
abstract class AbstractCommandProcessor implements Processor {
    protected List<String> deprecatedVersions = new ArrayList<>();
    protected final ProcessorContext context;
    protected String version;

    protected static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    protected AbstractCommandProcessor(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MessageResponse process() {
        setDeprecatedVersions();
        version = VersionValidationUtils.validateVersion(deprecatedVersions, context.requestHeaders());
        return processCommand();
    }

    protected abstract void setDeprecatedVersions();

    protected abstract MessageResponse processCommand();
}
