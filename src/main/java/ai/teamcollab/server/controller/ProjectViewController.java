package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Conversation;
import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.dto.ConversationCreateRequest;
import ai.teamcollab.server.dto.ProjectCreateRequest;
import ai.teamcollab.server.service.ConversationService;
import ai.teamcollab.server.service.ProjectService;
import ai.teamcollab.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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

@Slf4j
@Controller
@RequestMapping("/projects")
@RequiredArgsConstructor
public class ProjectViewController {

    private final ProjectService projectService;
    private final ConversationService conversationService;
    private final UserService userService;

    @GetMapping
    public String index(@AuthenticationPrincipal LoginUserDetails user, Model model) {
        final var projects = projectService.getProjectsByCompany(user.getCompanyId());
        model.addAttribute("projects", projects);
        return "projects/index";
    }

    @GetMapping("/new")
    public String newProject(Model model) {
        model.addAttribute("projectCreateRequest", new ProjectCreateRequest());
        return "projects/new";
    }

    @PostMapping("/create")
    public String create(@Valid @ModelAttribute("projectCreateRequest") ProjectCreateRequest request,
                         BindingResult bindingResult, RedirectAttributes redirectAttributes, Model model) {

        if (bindingResult.hasErrors()) {
            return "projects/new";
        }

        try {
            final var response = projectService.createProject(request);
            redirectAttributes.addFlashAttribute("successMessage", "Project created successfully");
            return "redirect:/conversations/" + response.getConversation().getId();
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Failed to create project: " + e.getMessage());
            return "projects/new";
        }
    }

    @GetMapping("/{projectId}")
    public String show(@PathVariable Long projectId,
                       @AuthenticationPrincipal LoginUserDetails user,
                       Model model,
                       RedirectAttributes redirectAttributes) {
        try {
            final var project = projectService.getProjectById(projectId, user.getCompanyId());
            model.addAttribute("project", project);
            var conversationRequest = new ConversationCreateRequest();
            conversationRequest.setProjectId(projectId);
            model.addAttribute("conversationCreateRequest", conversationRequest);
            return "projects/show";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Project not found");
            return "redirect:/projects";
        } catch (IllegalStateException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "You don't have access to this project");
            return "redirect:/projects";
        }
    }

    @PostMapping("/{projectId}/conversations")
    public String createConversation(
            @PathVariable Long projectId,
            @AuthenticationPrincipal LoginUserDetails userLogin,
            @Valid @ModelAttribute("conversationCreateRequest") ConversationCreateRequest request,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes,
            Model model) {

        log.debug("[DEBUG_LOG] Starting conversation creation for project {} by user {}", projectId, userLogin.getUsername());
        log.debug("[DEBUG_LOG] Conversation purpose: {}", request.getPurpose());

        if (bindingResult.hasErrors()) {
            log.debug("[DEBUG_LOG] Validation errors found: {}", bindingResult.getAllErrors());
            try {
                final var project = projectService.getProjectById(projectId, userLogin.getCompanyId());
                model.addAttribute("project", project);
                model.addAttribute("conversationCreateRequest", request);
                return "projects/show";
            } catch (Exception e) {
                log.error("[DEBUG_LOG] Error fetching project: {}", e.getMessage());
                redirectAttributes.addFlashAttribute("errorMessage", "Project not found");
                return "redirect:/projects";
            }
        }

        try {
            log.debug("[DEBUG_LOG] Fetching project entity for ID: {}", projectId);
            var project = projectService.getProjectEntityById(projectId, userLogin.getCompanyId());

            log.debug("[DEBUG_LOG] Creating new conversation");
            final var user = userService.getUserById(userLogin.getId());
            var conversation = new Conversation(request.getPurpose(), user);
            conversation.setProject(project);

            log.debug("[DEBUG_LOG] Saving conversation");
            var savedConversation = conversationService.createConversation(conversation, userLogin.getId());
            log.debug("[DEBUG_LOG] Conversation saved with ID: {}", savedConversation.getId());

            redirectAttributes.addFlashAttribute("successMessage", "Conversation created successfully");
            var redirectUrl = "redirect:/conversations/" + savedConversation.getId();
            log.debug("[DEBUG_LOG] Redirecting to: {}", redirectUrl);
            return redirectUrl;
        } catch (Exception e) {
            log.error("[DEBUG_LOG] Error creating conversation: ", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create conversation: " + e.getMessage());
            return "redirect:/projects/" + projectId;
        }
    }
}
