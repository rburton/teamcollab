package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Role;
import ai.teamcollab.server.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Service
@RequiredArgsConstructor
public class RoleService {

    private final RoleRepository roleRepository;

    @Transactional(readOnly = true)
    public List<Role> getAllRoles() {
        return StreamSupport.stream(roleRepository.findAll().spliterator(), false)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Role getRoleByName(String name) {
        return roleRepository.findByName(name)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + name));
    }
}