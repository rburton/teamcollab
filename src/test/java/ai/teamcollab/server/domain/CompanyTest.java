package ai.teamcollab.server.domain;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class CompanyTest {

    private Validator validator;
    private Company company;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
        company = new Company("Test Company");
    }

    @Test
    void defaultConstructor_ShouldCreateInstance() {
        Company company = new Company();
        assertNotNull(company);
        assertNull(company.getId());
        assertNull(company.getName());
        assertNotNull(company.getUsers());
        assertTrue(company.getUsers().isEmpty());
    }

    @Test
    void parameterizedConstructor_ShouldInitializeFieldsCorrectly() {
        assertEquals("Test Company", company.getName());
        assertNull(company.getId());
        assertNotNull(company.getUsers());
        assertTrue(company.getUsers().isEmpty());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void name_WhenBlank_ShouldFailValidation(String name) {
        company.setName(name);
        var violations = validator.validate(company);
        assertFalse(violations.isEmpty());
        assertTrue(violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("name")));
    }

    @Test
    void setAndGetName_ShouldWorkCorrectly() {
        company.setName("New Company Name");
        assertEquals("New Company Name", company.getName());
    }

    @Test
    void setAndGetId_ShouldWorkCorrectly() {
        assertNull(company.getId());
        
        company.setId(1L);
        assertEquals(1L, company.getId());
    }

    @Test
    void addUser_ShouldAddUserToSet() {
        User user = new User("testuser", "password", "test@example.com");
        
        company.addUser(user);
        
        assertEquals(1, company.getUsers().size());
        assertTrue(company.getUsers().contains(user));
    }

    @Test
    void setUsers_ShouldReplaceExistingUsers() {
        User user1 = new User("user1", "password", "user1@example.com");
        company.addUser(user1);

        Set<User> newUsers = new HashSet<>();
        User user2 = new User("user2", "password", "user2@example.com");
        newUsers.add(user2);

        company.setUsers(newUsers);

        assertEquals(1, company.getUsers().size());
        assertTrue(company.getUsers().contains(user2));
        assertFalse(company.getUsers().contains(user1));
    }

    @Test
    void owns_WhenPersonaBelongsToCompany_ShouldReturnTrue() {
        Persona persona = new Persona();
        persona.setCompany(company);

        assertTrue(company.owns(persona));
    }

    @Test
    void owns_WhenPersonaBelongsToOtherCompany_ShouldReturnFalse() {
        Company otherCompany = new Company("Other Company");
        Persona persona = new Persona();
        persona.setCompany(otherCompany);

        assertFalse(company.owns(persona));
    }

    @Test
    void owns_WhenPersonaHasNoCompany_ShouldReturnFalse() {
        Persona persona = new Persona();
        assertFalse(company.owns(persona));
    }

    @Test
    void doesntOwns_ShouldReturnOppositeOfOwns() {
        Persona ownedPersona = new Persona();
        ownedPersona.setCompany(company);
        assertFalse(company.doesntOwns(ownedPersona));

        Persona notOwnedPersona = new Persona();
        assertTrue(company.doesntOwns(notOwnedPersona));
    }

    @Test
    void validCompany_ShouldPassValidation() {
        var violations = validator.validate(company);
        assertTrue(violations.isEmpty());
    }
}