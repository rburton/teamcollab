package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectService projectService;


    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal Authentication authentication, Model model) {
        final var user = (User) authentication.getPrincipal();
        final var projects = projectService.getProjectsByCompany(user.getCompany().getId());
        model.addAttribute("projects", projects);
        return "dashboard/dashboard";
    }
}