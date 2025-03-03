package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Role;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.CompanyRepository;
import ai.teamcollab.server.repository.RoleRepository;
import ai.teamcollab.server.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private RoleRepository roleRepository;
    @Mock
    private CompanyRepository companyRepository;
    @Mock
    private PasswordEncoder passwordEncoder;

    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userService = new UserService(userRepository, roleRepository, companyRepository, passwordEncoder);
    }

    @Test
    void loadUserByUsername_WhenUserExists_ReturnsUser() {
        // Arrange
        User expectedUser = new User();
        expectedUser.setUsername("testuser");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(expectedUser));

        // Act
        User actualUser = (User) userService.loadUserByUsername("testuser");

        // Assert
        assertNotNull(actualUser);
        assertEquals("testuser", actualUser.getUsername());
        verify(userRepository).findByUsername("testuser");
    }

    @Test
    void loadUserByUsername_WhenUserNotFound_ThrowsException() {
        // Arrange
        when(userRepository.findByUsername("nonexistent")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(UsernameNotFoundException.class,
                () -> userService.loadUserByUsername("nonexistent"));
        verify(userRepository).findByUsername("nonexistent");
    }

    @Test
    void registerNewUser_WhenValid_ReturnsUser() {
        // Arrange
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("newuser@example.com");
        user.setPassword("password");

        Role role = new Role();
        role.setName("USER");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("USER")).thenReturn(Optional.of(role));
        when(passwordEncoder.encode(anyString())).thenReturn("encodedPassword");
        when(userRepository.save(any(User.class))).thenReturn(user);

        // Act
        User savedUser = userService.registerNewUser(user, "USER");

        // Assert
        assertNotNull(savedUser);
        verify(passwordEncoder).encode("password");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerNewUser_WhenUsernameExists_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("existinguser");
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(user, "USER"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUser_WhenEmailExists_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("existing@example.com");
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(user, "USER"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUser_WhenCompanyNotFound_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");
        Company company = new Company();
        company.setId(1L);
        user.setCompany(company);

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(companyRepository.existsById(1L)).thenReturn(false);

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
                () -> userService.registerNewUser(user, "USER"));
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerNewUser_WhenRoleNotFound_ThrowsException() {
        // Arrange
        User user = new User();
        user.setUsername("newuser");
        user.setEmail("new@example.com");

        when(userRepository.existsByUsername(anyString())).thenReturn(false);
        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(roleRepository.findByName("INVALID_ROLE")).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(IllegalStateException.class,
                () -> userService.registerNewUser(user, "INVALID_ROLE"));
        verify(userRepository, never()).save(any(User.class));
    }

    @ParameterizedTest
    @ValueSource(strings = {"existinguser", "anotheruser"})
    void existsByUsername_ReturnsExpectedResult(String username) {
        // Arrange
        when(userRepository.existsByUsername(username)).thenReturn(true);

        // Act
        boolean exists = userService.existsByUsername(username);

        // Assert
        assertTrue(exists);
        verify(userRepository).existsByUsername(username);
    }

    @Test
    void getUserStats_ReturnsCorrectStats() {
        // Arrange
        Long companyId = 1L;
        when(userRepository.countByCompanyIdAndEnabledTrue(companyId)).thenReturn(3L);
        when(userRepository.countByCompanyIdAndEnabledFalse(companyId)).thenReturn(2L);

        // Act
        var stats = userService.getUserStats(companyId);

        // Assert
        assertNotNull(stats);
        assertEquals(3L, stats.activeUsers());
        assertEquals(2L, stats.disabledUsers());
        assertEquals(5L, stats.totalUsers());
        verify(userRepository).countByCompanyIdAndEnabledTrue(companyId);
        verify(userRepository).countByCompanyIdAndEnabledFalse(companyId);
    }

    @ParameterizedTest
    @ValueSource(strings = {"existing@example.com", "another@example.com"})
    void existsByEmail_ReturnsExpectedResult(String email) {
        // Arrange
        when(userRepository.existsByEmail(email)).thenReturn(true);

        // Act
        boolean exists = userService.existsByEmail(email);

        // Assert
        assertTrue(exists);
        verify(userRepository).existsByEmail(email);
    }
}
