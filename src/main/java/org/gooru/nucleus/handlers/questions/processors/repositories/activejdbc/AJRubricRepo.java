package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.RubricRepo;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbhandlers.DBHandlerBuilder;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.transactions.TransactionExecutor;
import org.gooru.nucleus.handlers.questions.processors.responses.MessageResponse;

/**
 * @author szgooru
 * Created On: 24-Feb-2017
 */
public class AJRubricRepo implements RubricRepo {

    private final ProcessorContext context;
    
    public AJRubricRepo(ProcessorContext context) {
        this.context = context;
    }

    @Override
    public MessageResponse updateRubric() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildUpdateRubricHandler(context));
    }

    @Override
    public MessageResponse fetchRubric() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildFetchRubricHandler(context));
    }

    @Override
    public MessageResponse createRubric() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildCreateRubricHandler(context));
    }

    @Override
    public MessageResponse deleteRubric() {
        return TransactionExecutor.executeTransaction(DBHandlerBuilder.buildDeleteRubricHandler(context));
    }

}
