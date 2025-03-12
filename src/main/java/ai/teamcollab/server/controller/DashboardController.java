package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.service.ProjectService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class DashboardController {

    private final ProjectService projectService;


    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal LoginUserDetails loginUserDetails, Model model) {
        final var projects = projectService.getProjectsByCompany(loginUserDetails.getCompanyId());
        model.addAttribute("projects", projects);
        return "dashboard/dashboard";
    }
}
