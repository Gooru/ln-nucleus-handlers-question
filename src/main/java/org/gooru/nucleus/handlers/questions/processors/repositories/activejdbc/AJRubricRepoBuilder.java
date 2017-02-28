package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RubricRepo;

/**
 * @author szgooru
 * Created On: 24-Feb-2017
 */
public class AJRubricRepoBuilder {

    private AJRubricRepoBuilder() {
        throw new AssertionError();
    }
    
    public static RubricRepo buildRubricRepo(ProcessorContext context) {
        return new AJRubricRepo(context);
    }

}
