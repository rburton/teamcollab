package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    private final UserService userService;
    private final CompanyService companyService;

    public AuthController(UserService userService, CompanyService companyService) {
        this.userService = userService;
        this.companyService = companyService;
    }

    @GetMapping("/login")
    public String login() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        model.addAttribute("company", new Company());
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") User user,
                             BindingResult userResult,
                             @Valid @ModelAttribute("company") Company company,
                             BindingResult companyResult,
                             RedirectAttributes redirectAttributes) {

        if (userResult.hasErrors() || companyResult.hasErrors()) {
            return "auth/register";
        }

        try {
            final var savedCompany = companyService.createCompany(company);

            user.setCompany(savedCompany);
            final var savedUser = userService.registerNewUser(user, "USER");

            companyService.addUserToCompany(savedCompany, savedUser);

            final var authentication = new UsernamePasswordAuthenticationToken(
                savedUser,
                null,
                savedUser.getAuthorities()
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            return "redirect:/dashboard";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/register";
        }
    }
}
