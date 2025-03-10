package ai.teamcollab.server.controller;

import ai.teamcollab.server.service.CompanyService;
import ai.teamcollab.server.service.SystemSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/system/admin")
@PreAuthorize("hasRole('SUPER_ADMIN')")
@RequiredArgsConstructor
public class SystemAdminController {

    private final CompanyService companyService;
    private final SystemSettingsService systemSettingsService;

    @GetMapping("/companies/{companyId}/settings")
    public String showCompanySettings(@PathVariable Long companyId, Model model) {
        try {
            model.addAttribute("company", companyService.getCompanyById(companyId));
            model.addAttribute("systemSettings", systemSettingsService.getCurrentSettings());
            return "system/admin/company-settings";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "error/not-found";
        }
    }

    @PostMapping("/companies/{companyId}/settings/llm-model")
    public String updateCompanyLlmModel(
            @PathVariable Long companyId,
            @RequestParam(required = false) String llmModel,
            RedirectAttributes redirectAttributes) {

        try {
            companyService.updateCompanyLlmModel(companyId, llmModel);
            redirectAttributes.addFlashAttribute("successMessage", 
                    llmModel == null ? "Company LLM model reset to system default" : "Company LLM model updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/system/admin/companies/" + companyId + "/settings";
    }

    @PostMapping("/companies/{companyId}/settings/monthly-spending-limit")
    public String updateCompanyMonthlySpendingLimit(
            @PathVariable Long companyId,
            @RequestParam BigDecimal monthlySpendingLimit,
            RedirectAttributes redirectAttributes) {

        try {
            companyService.updateCompanyMonthlySpendingLimit(companyId, monthlySpendingLimit);
            redirectAttributes.addFlashAttribute("successMessage", "Monthly spending limit updated successfully");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }

        return "redirect:/system/admin/companies/" + companyId + "/settings";
    }
}
