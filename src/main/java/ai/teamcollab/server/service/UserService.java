package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.domain.Role;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.UserStats;
import ai.teamcollab.server.repository.CompanyRepository;
import ai.teamcollab.server.repository.RoleRepository;
import ai.teamcollab.server.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ai.teamcollab.server.domain.Audit.AuditActionType.ACCOUNT_DISABLED;
import static ai.teamcollab.server.domain.Audit.AuditActionType.ACCOUNT_ENABLED;
import static ai.teamcollab.server.domain.Audit.AuditActionType.EMAIL_CHANGED;
import static ai.teamcollab.server.domain.Audit.AuditActionType.PASSWORD_CHANGED;
import static ai.teamcollab.server.domain.Audit.AuditActionType.ROLES_CHANGED;
import static ai.teamcollab.server.domain.Audit.AuditActionType.USER_CREATED;
import static java.util.Objects.nonNull;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public UserService(UserRepository userRepository,
                       RoleRepository roleRepository,
                       CompanyRepository companyRepository,
                       PasswordEncoder passwordEncoder,
                       AuditService auditService) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new LoginUserDetails(user);
    }

    @Transactional
    public User registerNewUser(User user, String... roleNames) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        final var company = user.getCompany();
        // Validate company exists
        if (nonNull(company) && !companyRepository.existsById(company.getId())) {
            throw new IllegalArgumentException("Company not found with id: " + company.getId());
        }

        // Encode password
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Add roles
        for (String roleName : roleNames) {
            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
            user.addRole(role);
        }

        final var savedUser = userRepository.save(user);

        // Create audit event for user creation
        auditService.createAuditEvent(
                USER_CREATED,
                savedUser,
                "User registered with roles: " + String.join(", ", roleNames),
                "User",
                savedUser.getId()
        );

        return savedUser;
    }

    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }

    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    @Transactional(readOnly = true)
    public List<User> getUsersByCompany(Long companyId) {
        return userRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));
    }

    @Transactional
    public User updateUserRoles(Long userId, Set<String> roleNames) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Get the old roles for audit details
        final var oldRoleNames = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        final var roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        final var savedUser = userRepository.save(user);

        // Create audit event for role change
        auditService.createAuditEvent(
                ROLES_CHANGED,
                savedUser,
                "User roles changed from " + oldRoleNames + " to " + roleNames,
                "User",
                savedUser.getId()
        );

        return savedUser;
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        final var newStatus = !user.isEnabled();
        user.setEnabled(newStatus);
        final var savedUser = userRepository.save(user);

        // Create audit event for account status change
        final var actionType = newStatus ? ACCOUNT_ENABLED : ACCOUNT_DISABLED;

        auditService.createAuditEvent(
                actionType,
                savedUser,
                "User account " + (newStatus ? "enabled" : "disabled"),
                "User",
                savedUser.getId()
        );

        return savedUser;
    }

    @Transactional(readOnly = true)
    public UserStats getUserStats(Long companyId) {
        return UserStats.builder()
                .activeUsers(userRepository.countByCompanyIdAndEnabledTrue(companyId))
                .disabledUsers(userRepository.countByCompanyIdAndEnabledFalse(companyId))
                .build();
    }

    @Transactional
    public User createCompanyUser(User user, Long companyId, Set<String> roleNames) {
        var company = companyRepository.findById(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + companyId));

        user.setCompany(company);
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        final var roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(roles);

        if (existsByUsername(user.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (existsByEmail(user.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        final var savedUser = userRepository.save(user);

        // Create audit event for user creation
        auditService.createAuditEvent(
                USER_CREATED,
                savedUser,
                "User created for company: " + company.getName() + " with roles: " + roleNames,
                "User",
                savedUser.getId()
        );

        return savedUser;
    }

    @Transactional
    public User updateUserBasicInfo(Long userId, String username, String email) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Store old values for audit
        final var oldUsername = user.getUsername();
        final var oldEmail = user.getEmail();
        boolean usernameChanged = !oldUsername.equals(username);
        boolean emailChanged = !oldEmail.equals(email);

        // Check if username is being changed and if it's already taken
        if (usernameChanged && existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email is being changed and if it's already taken
        if (emailChanged && existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(username);
        user.setEmail(email);

        final var savedUser = userRepository.save(user);

        // Create audit event for email change if email was changed
        if (emailChanged) {
            auditService.createAuditEvent(
                    EMAIL_CHANGED,
                    savedUser,
                    "User email changed from " + oldEmail + " to " + email,
                    "User",
                    savedUser.getId()
            );
        }

        return savedUser;
    }

    @Transactional
    public User updateUserPassword(Long userId, String password) {
        final var user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Only update password if it's provided
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        final var savedUser = userRepository.save(user);

        // Create audit event for password change
        auditService.createAuditEvent(
                PASSWORD_CHANGED,
                savedUser,
                "User password changed",
                "User",
                savedUser.getId()
        );

        return savedUser;
    }

    /**
     * @deprecated Use updateUserBasicInfo and updateUserPassword instead
     */
    @Deprecated
    @Transactional
    public User updateUserProfile(Long userId, String username, String email, String password) {
        var user = updateUserBasicInfo(userId, username, email);

        // Only update password if it's provided
        if (password != null && !password.isEmpty()) {
            user = updateUserPassword(userId, password);
        }

        return user;
    }
}
