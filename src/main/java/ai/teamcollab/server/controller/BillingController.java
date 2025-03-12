package ai.teamcollab.server.controller;

import ai.teamcollab.server.domain.LoginUserDetails;
import ai.teamcollab.server.domain.Subscription;
import ai.teamcollab.server.service.PlanService;
import ai.teamcollab.server.service.SubscriptionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Slf4j
@Controller
@RequestMapping("/company/billing")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class BillingController {

    private final PlanService planService;
    private final SubscriptionService subscriptionService;

    @GetMapping("")
    public String billingPage(@AuthenticationPrincipal LoginUserDetails currentUser, Model model) {
        log.info("Displaying billing page for company: {}", currentUser.getCompanyId());
        final var companyId = currentUser.getCompanyId();
        final var plans = planService.getAllPlansWithDetails();

        // Get current subscription if exists
        Subscription currentSubscription = null;
        try {
            currentSubscription = subscriptionService.getActiveSubscription(companyId);
        } catch (Exception e) {
            // No active subscription found, which is fine
            log.debug("No active subscription found for company: {}", companyId);
        }

        model.addAttribute("plans", plans);
        model.addAttribute("currentSubscription", currentSubscription);

        return "company/billing";
    }

    @PostMapping("/subscribe")
    public String subscribe(@RequestParam Long planId,
                            @AuthenticationPrincipal LoginUserDetails currentUser,
                            RedirectAttributes redirectAttributes) {

        try {
            final var companyId = currentUser.getCompanyId();
            log.info("Creating subscription for company: {} with plan: {}", companyId, planId);

            // In a real implementation, this would create a Stripe checkout session
            // and redirect the user to Stripe for payment
            // For now, we'll just create a subscription with a dummy Stripe ID
            final var stripeSubscriptionId = "sub_" + System.currentTimeMillis();
            subscriptionService.createSubscription(companyId, planId, stripeSubscriptionId);

            redirectAttributes.addFlashAttribute("successMessage", "Subscription created successfully");
        } catch (Exception e) {
            log.error("Failed to create subscription", e);
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create subscription: " + e.getMessage());
        }

        return "redirect:/company/billing";
    }
}
