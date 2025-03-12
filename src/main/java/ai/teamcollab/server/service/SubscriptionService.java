package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Audit;
import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Plan;
import ai.teamcollab.server.domain.Subscription;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.CompanyRepository;
import ai.teamcollab.server.repository.SubscriptionRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionService {

    private final SubscriptionRepository subscriptionRepository;
    private final CompanyRepository companyRepository;
    private final PlanService planService;
    private final AuditService auditService;

    @Transactional(readOnly = true)
    public List<Subscription> getCompanySubscriptions(Long companyId) {
        return subscriptionRepository.findByCompanyId(companyId);
    }

    @Transactional(readOnly = true)
    public Subscription getActiveSubscription(Long companyId) {
        return subscriptionRepository.findActiveSubscriptionForCompany(companyId, LocalDate.now())
            .orElseThrow(() -> new EntityNotFoundException("No active subscription found for company: " + companyId));
    }

    @Transactional
    public Subscription createSubscription(Long companyId, Long planId, String stripeSubscriptionId) {
        log.info("Creating subscription for company: {} with plan: {}", companyId, planId);

        Company company = companyRepository.findById(companyId)
            .orElseThrow(() -> new EntityNotFoundException("Company not found with id: " + companyId));

        Plan plan = planService.getPlanById(planId);

        var subscription = Subscription.builder()
            .company(company)
            .plan(plan)
            .stripeSubscriptionId(stripeSubscriptionId)
            .startDate(LocalDate.now())
            .build();

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Create audit event for subscription creation
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditEvent(
                Audit.AuditActionType.SUBSCRIPTION_CREATED,
                currentUser,
                "Subscription created for company: " + company.getName() + " with plan: " + plan.getName(),
                "Subscription",
                savedSubscription.getId()
            );
        }

        return savedSubscription;
    }

    /**
     * Get the currently authenticated user.
     *
     * @return the current user or null if not authenticated
     */
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof User) {
            return (User) authentication.getPrincipal();
        }
        return null;
    }

    @Transactional
    public Subscription cancelSubscription(Long subscriptionId, LocalDate endDate) {
        log.info("Cancelling subscription: {} effective from: {}", subscriptionId, endDate);

        var subscription = subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + subscriptionId));

        subscription.setEndDate(endDate);

        Subscription savedSubscription = subscriptionRepository.save(subscription);

        // Create audit event for subscription cancellation
        User currentUser = getCurrentUser();
        if (currentUser != null) {
            auditService.createAuditEvent(
                Audit.AuditActionType.SUBSCRIPTION_CHANGED,
                currentUser,
                "Subscription cancelled for company: " + subscription.getCompany().getName() + 
                " with plan: " + subscription.getPlan().getName() + " effective from: " + endDate,
                "Subscription",
                savedSubscription.getId()
            );
        }

        return savedSubscription;
    }

    @Transactional(readOnly = true)
    public Subscription findByStripeSubscriptionId(String stripeSubscriptionId) {
        return subscriptionRepository.findByStripeSubscriptionId(stripeSubscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription not found with Stripe ID: " + stripeSubscriptionId));
    }

    @Transactional(readOnly = true)
    public Subscription findById(Long subscriptionId) {
        return subscriptionRepository.findById(subscriptionId)
            .orElseThrow(() -> new EntityNotFoundException("Subscription not found with id: " + subscriptionId));
    }
}
