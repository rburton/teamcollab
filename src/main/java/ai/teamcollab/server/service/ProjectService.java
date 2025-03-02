package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Project;
import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectCreateRequest;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.repository.ProjectRepository;
import ai.teamcollab.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    @Transactional
    public ProjectResponse createProject(ProjectCreateRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        Project project = new Project();
        project.setName(request.getName());
        project.setTopic(request.getTopic());
        project.setCreatedAt(LocalDateTime.now());

        Conversation conversation = new Conversation(request.getPurpose(), user);
        conversation.setProject(project);
        project.getConversations().add(conversation);

        project = projectRepository.save(project);
        
        return buildProjectResponse(project);
    }

    private ProjectResponse buildProjectResponse(Project project) {
        Conversation conversation = project.getConversations().iterator().next();
        
        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .topic(project.getTopic())
                .createdAt(project.getCreatedAt())
                .conversation(ProjectResponse.ConversationResponse.builder()
                        .id(conversation.getId())
                        .purpose(conversation.getPurpose())
                        .createdAt(conversation.getCreatedAt())
                        .build())
                .build();
    }
}