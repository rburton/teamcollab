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

import static java.util.Objects.nonNull;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CompanyRepository companyRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,
                      RoleRepository roleRepository,
                      CompanyRepository companyRepository,
                      PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.companyRepository = companyRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        final var user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        return new LoginUserDetails(user);
    }

    @Transactional
    public User registerNewUser(User user, String roleName) {
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

        // Add default role
        Role role = roleRepository.findByName(roleName)
                .orElseThrow(() -> new IllegalStateException("Role not found: " + roleName));
        user.addRole(role);

        return userRepository.save(user);
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        Set<Role> roles = roleNames.stream()
                .map(name -> roleRepository.findByName(name)
                        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name)))
                .collect(Collectors.toSet());

        user.setRoles(roles);
        return userRepository.save(user);
    }

    @Transactional
    public User toggleUserStatus(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        user.setEnabled(!user.isEnabled());
        return userRepository.save(user);
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

        Set<Role> roles = roleNames.stream()
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

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserBasicInfo(Long userId, String username, String email) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Check if username is being changed and if it's already taken
        if (!user.getUsername().equals(username) && existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }

        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(email) && existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(username);
        user.setEmail(email);

        return userRepository.save(user);
    }

    @Transactional
    public User updateUserPassword(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + userId));

        // Only update password if it's provided
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        } else {
            throw new IllegalArgumentException("Password cannot be empty");
        }

        return userRepository.save(user);
    }

    /**
     * @deprecated Use updateUserBasicInfo and updateUserPassword instead
     */
    @Deprecated
    @Transactional
    public User updateUserProfile(Long userId, String username, String email, String password) {
        User user = updateUserBasicInfo(userId, username, email);

        // Only update password if it's provided
        if (password != null && !password.isEmpty()) {
            user = updateUserPassword(userId, password);
        }

        return user;
    }
}
