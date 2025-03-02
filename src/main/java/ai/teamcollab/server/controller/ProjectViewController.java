package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.ProjectCreateRequest;
import ai.teamcollab.server.dto.ProjectResponse;
import ai.teamcollab.server.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

    @GetMapping("/new")
    public String newProject(Model model) {
        model.addAttribute("projectCreateRequest", new ProjectCreateRequest());
        return "projects/new";
    }

    @PostMapping("/create")
    public String create(
            @AuthenticationPrincipal User user,
            @Valid @ModelAttribute("projectCreateRequest") ProjectCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (bindingResult.hasErrors()) {
            return "projects/new";
        }

        try {
            ProjectResponse response = projectService.createProject(request);
            redirectAttributes.addFlashAttribute("successMessage", "Project created successfully");
            return "redirect:/conversations/" + response.getConversation().getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create project: " + e.getMessage());
            return "projects/new";
        }
    }

    @GetMapping("/{projectId}")
    public String show(@PathVariable Long projectId, 
                      @AuthenticationPrincipal User user,
                      Model model,
                      RedirectAttributes redirectAttributes) {
        try {
            ProjectResponse project = projectService.getProjectById(projectId, user.getCompany().getId());
            model.addAttribute("project", project);
            return "projects/show";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found");
            return "redirect:/projects";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have access to this project");
            return "redirect:/projects";
        }
    }
}
