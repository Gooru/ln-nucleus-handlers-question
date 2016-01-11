package org.gooru.nucleus.handlers.questions.processors;

import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

public interface Processor {
  public MessageResponse process();
}
