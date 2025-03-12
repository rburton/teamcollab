package ai.teamcollab.server.security;

import ai.teamcollab.server.domain.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.io.Serializable;
import java.util.Collection;
import java.util.Map;

/**
 * Custom implementation of User that also implements OAuth2User.
 * This allows the User entity to be accessed through the principal of the authentication object
 * for both OAuth2 authentication and username/password authentication.
 */
public class UserOAuth2User extends User implements OAuth2User, Serializable {

    private final OAuth2User oauth2User;

    public UserOAuth2User(OAuth2User oauth2User, User user) {
        // Copy all properties from the user to this instance
        super(user.getUsername(), user.getPassword(), user.getEmail());
        this.setId(user.getId());
        this.setEnabled(user.isEnabled());
        this.setCompany(user.getCompany());
        this.setRoles(user.getRoles());
        this.oauth2User = oauth2User;
    }

    @Override
    public Map<String, Object> getAttributes() {
        return oauth2User.getAttributes();
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return super.getAuthorities();
    }

    @Override
    public String getName() {
        return getUsername();
    }
}
