package ai.teamcollab.server.domain;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * Custom implementation of UserDetails that includes user id, username, roles, and company id.
 * This class wraps a User object and delegates most of the UserDetails methods to it.
 */
@Getter
public class LoginUserDetails implements OAuth2User, UserDetails {

    private final Long id;
    private final String username;
    private final Set<Role> roles;
    private final Long companyId;
    private final String companyName;
    private final User user;

    public LoginUserDetails(User user) {
        this.user = user;
        this.id = user.getId();
        this.username = user.getUsername();
        this.roles = user.getRoles();
        this.companyId = user.getCompany() != null ? user.getCompany().getId() : null;
        this.companyName = user.getCompany() != null ? user.getCompany().getName() : null;
    }


    @Override
    public <A> A getAttribute(String name) {
        return OAuth2User.super.getAttribute(name);
    }

    @Override
    public Map<String, Object> getAttributes() {
        return Map.of();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return user.getAuthorities();
    }

    @Override
    public String getPassword() {
        return "";
    }

    @Override
    public boolean isAccountNonExpired() {
        return UserDetails.super.isAccountNonExpired();
    }

    @Override
    public boolean isAccountNonLocked() {
        return UserDetails.super.isAccountNonLocked();
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return UserDetails.super.isCredentialsNonExpired();
    }

    @Override
    public boolean isEnabled() {
        return UserDetails.super.isEnabled();
    }

    @Override
    public String getName() {
        return username;
    }
}