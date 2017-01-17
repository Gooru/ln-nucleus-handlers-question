package org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.dbauth;

import org.gooru.nucleus.handlers.questions.processors.ProcessorContext;
import org.gooru.nucleus.handlers.questions.processors.repositories.activejdbc.entities.AJEntityQuestion;

/**
 * Created by ashish on 16/1/17.
 */
public final class AuthorizerBuilder {

    private AuthorizerBuilder() {
        throw new AssertionError();
    }

    public static Authorizer<AJEntityQuestion> buildTenantAuthorizer(ProcessorContext context) {
        return new TenantAuthorizer(context);
    }
}
