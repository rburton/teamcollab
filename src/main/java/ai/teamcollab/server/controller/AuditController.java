package ai.teamcollab.server.controller;

import ai.teamcollab.server.controller.domain.AuditView;
import ai.teamcollab.server.domain.Audit;
import ai.teamcollab.server.service.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Controller for handling audit-related requests. This controller is only accessible to users with the SUPER_ADMIN
 * role.
 */
@Controller
@RequestMapping("/audits")
@RequiredArgsConstructor
public class AuditController {

    private final AuditService auditService;

    /**
     * Display the audit dashboard with categories.
     *
     * @param page the page number (0-based)
     * @param size the page size
     * @param model the model to add attributes to
     * @return the view name
     */
    @GetMapping
    public String showAuditDashboard(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        // Get all audit action types
        final var auditCategories = Arrays.stream(Audit.AuditActionType.values())
                .map(Enum::name)
                .collect(Collectors.toList());

        model.addAttribute("auditCategories", auditCategories);

        // Get recent audits for the dashboard with pagination
        final var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
        final var auditPage = auditService.findMostRecentAudits(pageable);

        model.addAttribute("recentAudits", auditPage.getContent().stream()
                .map(AuditView::from)
                .toList());
        model.addAttribute("currentPage", auditPage.getNumber());
        model.addAttribute("totalPages", auditPage.getTotalPages());
        model.addAttribute("totalItems", auditPage.getTotalElements());
        model.addAttribute("pageSize", size);

        return "audits/index";
    }

    /**
     * Display audits for a specific category.
     *
     * @param category the audit category to display
     * @param page the page number (0-based)
     * @param size the page size
     * @param model the model to add attributes to
     * @return the view name
     */
    @GetMapping("/category")
    public String showAuditsByCategory(
            @RequestParam("type") String category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        try {
            final var actionType = Audit.AuditActionType.valueOf(category);
            final var pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "timestamp"));
            final var auditPage = auditService.findAuditsByActionType(actionType, pageable);

            model.addAttribute("categoryName", category);
            model.addAttribute("audits", auditPage.getContent().stream()
                    .map(AuditView::from)
                    .toList());
            model.addAttribute("currentPage", auditPage.getNumber());
            model.addAttribute("totalPages", auditPage.getTotalPages());
            model.addAttribute("totalItems", auditPage.getTotalElements());
            model.addAttribute("pageSize", size);

            return "audits/category";
        } catch (IllegalArgumentException e) {
            // Handle invalid category
            return "redirect:/audits";
        }
    }
}
