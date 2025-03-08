package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.dto.CreateUserDTO;
import ai.teamcollab.server.service.RoleService;
import ai.teamcollab.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Set;

@Controller
@RequestMapping("/company/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class CompanyAdminController {

    private final UserService userService;
    private final RoleService roleService;

    @GetMapping("")
    public String dashboard(@AuthenticationPrincipal User currentUser, Model model) {
        var stats = userService.getUserStats(currentUser.getCompany().getId());
        model.addAttribute("stats", stats);
        return "company/admin/dashboard";
    }

    @GetMapping("/users")
    public String listUsers(@AuthenticationPrincipal User currentUser, Model model) {
        final var users = userService.getUsersByCompany(currentUser.getCompany().getId());
        model.addAttribute("users", users);
        model.addAttribute("allRoles", roleService.getAllRoles());
        return "company/admin/users/index";
    }

    @GetMapping("/users/new")
    public String showCreateUserForm(@AuthenticationPrincipal User currentUser, Model model) {
        model.addAttribute("user", new CreateUserDTO());
        model.addAttribute("roles", roleService.getAllRoles());
        return "company/admin/users/create";
    }

    @PostMapping("/users/new")
    public String createUser(
            @Valid @ModelAttribute("user") CreateUserDTO createUserDTO,
            BindingResult result,
            @RequestParam Set<String> roles,
            @AuthenticationPrincipal User currentUser,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("roles", roleService.getAllRoles());
            return "company/admin/users/create";
        }

        try {
            final var user = User.builder()
                    .username(createUserDTO.getUsername())
                    .email(createUserDTO.getEmail())
                    .password(createUserDTO.getPassword())
                    .enabled(true)
                    .build();

            userService.createCompanyUser(user, currentUser.getCompany().getId(), roles);
            redirectAttributes.addFlashAttribute("successMessage", "User created successfully");
            return "redirect:/company/admin/users";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/company/admin/users/new";
        }
    }

    @PostMapping("/users/{userId}/roles")
    public String updateUserRoles(
            @PathVariable Long userId,
            @RequestParam Set<String> roles,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            final var targetUser = userService.getUserById(userId);
            if (!targetUser.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new IllegalArgumentException("User not found in your company");
            }

            userService.updateUserRoles(userId, roles);
            redirectAttributes.addFlashAttribute("successMessage", "User roles updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/company/admin/users";
    }

    @PostMapping("/users/{userId}/toggle")
    public String toggleUserStatus(
            @PathVariable Long userId,
            @AuthenticationPrincipal User currentUser,
            RedirectAttributes redirectAttributes) {

        try {
            // Verify user belongs to current user's company
            final var targetUser = userService.getUserById(userId);
            if (!targetUser.getCompany().getId().equals(currentUser.getCompany().getId())) {
                throw new IllegalArgumentException("User not found in your company");
            }

            final var user = userService.toggleUserStatus(userId);
            final var status = user.isEnabled() ? "enabled" : "disabled";
            redirectAttributes.addFlashAttribute("successMessage",
                    "User account has been " + status);
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/company/admin/users";
    }

}
