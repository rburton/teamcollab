package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.SystemSettings;
import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.SystemSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
@RequestMapping("/system")
@RequiredArgsConstructor
@PreAuthorize("hasRole('SUPER_ADMIN')")
public class SystemController {

    private final SystemSettingsService systemSettingsService;
    private final CompanyService companyService;

    @GetMapping("/companies")
    public String listCompanies(Model model) {
        log.debug("Showing companies list page");
        final var companies = companyService.getAllCompanies();
        model.addAttribute("companies", companies);
        return "system/companies";
    }

    @GetMapping
    public String index() {
        log.debug("Showing system dashboard page");
        return "system/index";
    }

    @GetMapping("/settings")
    public String showSettings(Model model) {
        log.debug("Showing system settings page");
        final var settings = systemSettingsService.getCurrentSettings();
        model.addAttribute("settings", settings);
        return "system/settings";
    }

    @PostMapping("/update")
    public String updateSettings(@Valid SystemSettings settings, BindingResult result, Model model) {
        log.debug("Updating system settings: {}", settings);

        if (result.hasErrors()) {
            return "system/settings";
        }

        try {
            systemSettingsService.updateSettings(settings);
            model.addAttribute("success", "System settings updated successfully");
        } catch (Exception e) {
            log.error("Error updating system settings", e);
            model.addAttribute("error", "Failed to update system settings");
            return "system/settings";
        }

        return "redirect:/system/settings";
    }
}
