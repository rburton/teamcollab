package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class UserTest {

    private Validator validator;
    private User user;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        user = new User("testuser", "password", "test@example.com");
    }

    @Test
    void constructor_ShouldInitializeFieldsCorrectly() {
        assertEquals("testuser", user.getUsername());
        assertEquals("password", user.getPassword());
        assertEquals("test@example.com", user.getEmail());
        assertTrue(user.isEnabled());
        assertTrue(user.getRoles().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void username_WhenBlank_ShouldFailValidation(String username) {
        user.setUsername(username);
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void password_WhenBlank_ShouldFailValidation(String password) {
        user.setPassword(password);
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("password")));
    }

    @ParameterizedTest
    @NullAndEmptySource
    void email_WhenBlank_ShouldFailValidation(String email) {
        user.setEmail(email);
        var violations = validator.validate(user);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email")));
    }

    @Test
    void addRole_ShouldAddRoleToSet() {
        Role role = new Role();
        role.setName("USER");
        
        user.addRole(role);
        
        assertTrue(user.getRoles().contains(role));
        assertEquals(1, user.getRoles().size());
    }

    @Test
    void setRoles_ShouldReplaceExistingRoles() {
        Role role1 = new Role();
        role1.setName("USER");
        user.addRole(role1);

        Set<Role> newRoles = new HashSet<>();
        Role role2 = new Role();
        role2.setName("ADMIN");
        newRoles.add(role2);

        user.setRoles(newRoles);

        assertEquals(1, user.getRoles().size());
        assertTrue(user.getRoles().contains(role2));
        assertFalse(user.getRoles().contains(role1));
    }

    @Test
    void getAuthorities_ShouldReturnCorrectAuthorities() {
        Role role1 = new Role();
        role1.setName("USER");
        Role role2 = new Role();
        role2.setName("ADMIN");

        user.addRole(role1);
        user.addRole(role2);

        Collection<? extends GrantedAuthority> authorities = user.getAuthorities();

        assertEquals(2, authorities.size());
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_USER")));
        assertTrue(authorities.stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")));
    }

    @Test
    void userDetailsImplementation_ShouldReturnExpectedValues() {
        assertTrue(user.isAccountNonExpired());
        assertTrue(user.isAccountNonLocked());
        assertTrue(user.isCredentialsNonExpired());
        assertTrue(user.isEnabled());
    }

    @Test
    void setEnabled_ShouldUpdateEnabledStatus() {
        assertTrue(user.isEnabled()); // default value
        
        user.setEnabled(false);
        assertFalse(user.isEnabled());
        
        user.setEnabled(true);
        assertTrue(user.isEnabled());
    }

    @Test
    void companyAssociation_ShouldWorkCorrectly() {
        Company company = new Company();
        company.setId(1L);
        company.setName("Test Company");

        user.setCompany(company);

        assertNotNull(user.getCompany());
        assertEquals("Test Company", user.getCompany().getName());
        assertEquals(1L, user.getCompany().getId());
    }

    @Test
    void idManagement_ShouldWorkCorrectly() {
        assertNull(user.getId()); // should be null initially

        user.setId(1L);
        assertEquals(1L, user.getId());
    }
}