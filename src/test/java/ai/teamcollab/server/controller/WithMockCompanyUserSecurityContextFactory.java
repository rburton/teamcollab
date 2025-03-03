package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.test.context.support.WithSecurityContextFactory;

import java.util.Arrays;
import java.util.stream.Collectors;

public class WithMockCompanyUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCompanyUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCompanyUser annotation) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();

        Company company = new Company();
        company.setId(annotation.companyId());
        company.setName("Test Company");

        User user = new User();
        user.setUsername(annotation.username());
        user.setCompany(company);
        user.setEnabled(true);

        var authorities = Arrays.stream(annotation.roles())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toSet());

        Authentication auth = new UsernamePasswordAuthenticationToken(user, "password", authorities);
        context.setAuthentication(auth);

        return context;
    }
}