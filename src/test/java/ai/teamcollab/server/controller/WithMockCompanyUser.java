package ai.teamcollab.server.controller;

import org.springframework.security.test.context.support.WithSecurityContext;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@WithSecurityContext(factory = WithMockCompanyUserSecurityContextFactory.class)
public @interface WithMockCompanyUser {
    String username() default "admin";
    String[] roles() default {"ADMIN"};
    long companyId() default 1L;
}