package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Audit;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.Project;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectCreateRequest;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.dto.ProjectResponse.ConversationResponse;
import ai.teamcollab.server.repository.ProjectRepository;
import ai.teamcollab.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setOverview(request.getOverview());
        project.setCreatedAt(LocalDateTime.now());
        project.setCompany(user.getCompany());

        Conversation conversation = new Conversation(request.getPurpose(), user);
        conversation.setProject(project);
        project.getConversations().add(conversation);

        project = projectRepository.save(project);

        // Create audit event for project creation
        auditService.createAuditEvent(
            Audit.AuditActionType.PROJECT_CREATED,
            user,
            "Project created: " + project.getName(),
            "Project",
            project.getId()
        );

        return buildProjectResponse(project);
    }

    private ProjectResponse buildProjectResponse(Project project) {
        Conversation conversation = project.getConversations().iterator().next();

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .overview(project.getOverview())
                .createdAt(project.getCreatedAt())
                .conversation(ConversationResponse.builder()
                        .id(conversation.getId())
                        .purpose(conversation.getPurpose())
                        .createdAt(conversation.getCreatedAt())
                        .createdBy(conversation.getUser().getUsername())
                        .build())
                .conversations(project.getConversations().stream()
                        .map(c -> ConversationResponse.builder()
                                .id(c.getId())
                                .purpose(c.getPurpose())
                                .createdAt(c.getCreatedAt())
                                .build())
                        .collect(toList()))
                .build();
    }

    public List<ProjectResponse> getProjectsByCompany(Long companyId) {
        return projectRepository.findByCompanyId(companyId).stream()
                .map(this::buildProjectResponse)
                .collect(toList());
    }

    @Transactional(readOnly = true)
    public ProjectResponse getProjectById(Long projectId, Long companyId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!project.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("Unauthorized access to project");
        }

        return buildProjectResponse(project);
    }

    @Transactional(readOnly = true)
    public Project getProjectEntityById(Long projectId, Long companyId) {
        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new IllegalArgumentException("Project not found"));

        if (!project.getCompany().getId().equals(companyId)) {
            throw new IllegalStateException("Unauthorized access to project");
        }

        return project;
    }
}
