package org.gooru.nucleus.handlers.questions.processors;

import io.vertx.core.eventbus.Message;

public final class ProcessorBuilder {

  public ProcessorBuilder() {
    throw new AssertionError();
  }

  public static Processor build(Message<Object> message) {
    return new MessageProcessor(message);
  }
}
