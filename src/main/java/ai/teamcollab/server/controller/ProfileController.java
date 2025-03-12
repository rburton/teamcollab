package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public String showProfileForm(@AuthenticationPrincipal LoginUserDetails userDetails, Model model) {
        User user = userService.getUserById(userDetails.getId());
        model.addAttribute("user", user);
        // Add auth providers to the model
        model.addAttribute("authProviders", user.getAuthProviders());
        return "profile/edit";
    }

    @PostMapping("/basic-info")
    public String updateBasicInfo(@AuthenticationPrincipal LoginUserDetails userDetails,
                               @Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        try {
            // Update username and email
            userService.updateUserBasicInfo(
                userDetails.getId(),
                user.getUsername(),
                user.getEmail()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Profile information updated successfully");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }

    @PostMapping("/password")
    public String updatePassword(@AuthenticationPrincipal LoginUserDetails userDetails,
                               @RequestParam(required = false) String password,
                               RedirectAttributes redirectAttributes) {

        try {
            // Update password
            if (password != null && !password.isEmpty()) {
                userService.updateUserPassword(
                    userDetails.getId(),
                    password
                );
                redirectAttributes.addFlashAttribute("successMessage", "Password updated successfully");
            } else {
                redirectAttributes.addFlashAttribute("errorMessage", "Password cannot be empty");
            }
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }

    /**
     * @deprecated Use updateBasicInfo and updatePassword instead
     */
    @Deprecated
    @PostMapping
    public String updateProfile(@AuthenticationPrincipal LoginUserDetails userDetails,
                               @Valid @ModelAttribute("user") User user,
                               BindingResult bindingResult,
                               RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "profile/edit";
        }

        try {
            // Only update username, email, and password
            userService.updateUserProfile(
                userDetails.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPassword()
            );

            redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully");
            return "redirect:/profile";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/profile";
        }
    }
}
