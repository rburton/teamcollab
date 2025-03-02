package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectService projectService;

    @GetMapping
    public String index(@AuthenticationPrincipal User user, Model model) {
        List<ProjectResponse> projects = projectService.getProjectsByCompany(user.getCompany().getId());
        model.addAttribute("projects", projects);
        return "projects/index";
    }
}