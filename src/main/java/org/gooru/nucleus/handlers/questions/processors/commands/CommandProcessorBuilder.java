package org.gooru.nucleus.handlers.questions.processors.commands;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;
import org.gooru.nucleus.handlers.questions.constants.MessageConstants;
import org.gooru.nucleus.handlers.questions.processors.Processor;
import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponseFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author ashish on 2/1/17.
 */
public enum CommandProcessorBuilder {

  DEFAULT("default") {
    private final Logger LOGGER = LoggerFactory.getLogger(CommandProcessorBuilder.class);
    private final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle("messages");

    @Override
    public Processor build(ProcessorContext context) {
      return () -> {
        LOGGER.error("Invalid operation type passed in, not able to handle");
        return MessageResponseFactory
            .createInvalidRequestResponse(RESOURCE_BUNDLE.getString("invalid.operation"));
      };
    }
  },
  QUESTION_DELETE(MessageConstants.MSG_OP_QUESTION_DELETE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionDeleteProcessor(context);
    }
  },
  QUESTION_UPDATE(MessageConstants.MSG_OP_QUESTION_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionUpdateProcessor(context);
    }
  },
  QUESTION_CREATE(MessageConstants.MSG_OP_QUESTION_CREATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionCreateProcessor(context);
    }
  },
  QUESTION_GET(MessageConstants.MSG_OP_QUESTION_GET) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionGetProcessor(context);
    }
  },
  QUESTION_GET_BULK(MessageConstants.MSG_OP_QUESTION_GET_BULK) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionGetBulkProcessor(context);
    }
  },
  RUBRIC_DELETE(MessageConstants.MSG_OP_RUBRIC_DELETE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new RubricDeleteProcessor(context);
    }
  },
  RUBRIC_UPDATE(MessageConstants.MSG_OP_RUBRIC_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new RubricUpdateProcessor(context);
    }
  },
  RUBRIC_CREATE(MessageConstants.MSG_OP_RUBRIC_CREATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new RubricCreateProcessor(context);
    }
  },
  RUBRIC_GET(MessageConstants.MSG_OP_RUBRIC_GET) {
    @Override
    public Processor build(ProcessorContext context) {
      return new RubricGetProcessor(context);
    }
  },
  QUESTION_RUBRIC_ASSOCIATE(MessageConstants.MSG_OP_QUESTION_RUBRIC_ASSOCIATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionRubricAssociationProcessor(context);
    }
  },
  QUESTION_SCORE_UPDATE(MessageConstants.MSG_OP_QUESTION_SCORE_UPDATE) {
    @Override
    public Processor build(ProcessorContext context) {
      return new QuestionScoreUpdateProcessor(context);
    }

  };

  private String name;

  CommandProcessorBuilder(String name) {
    this.name = name;
  }

  public String getName() {
    return this.name;
  }

  private static final Map<String, CommandProcessorBuilder> LOOKUP = new HashMap<>();

  static {
    for (CommandProcessorBuilder builder : values()) {
      LOOKUP.put(builder.getName(), builder);
    }
  }

  public static CommandProcessorBuilder lookupBuilder(String name) {
    CommandProcessorBuilder builder = LOOKUP.get(name);
    if (builder == null) {
      return DEFAULT;
    }
    return builder;
  }

  public abstract Processor build(ProcessorContext context);
}
