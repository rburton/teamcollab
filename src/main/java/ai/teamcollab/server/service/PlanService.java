package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Plan;
import ai.teamcollab.server.domain.PlanDetail;
import ai.teamcollab.server.repository.PlanDetailRepository;
import ai.teamcollab.server.repository.PlanRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PlanService {

    private final PlanRepository planRepository;
    private final PlanDetailRepository planDetailRepository;

    @Transactional(readOnly = true)
    public List<Plan> getAllPlans() {
        return planRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Plan> getAllPlansWithDetails() {
        return planRepository.findAllWithDetails();
    }

    @Transactional(readOnly = true)
    public Plan getPlanById(Long planId) {
        return planRepository.findById(planId)
            .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));
    }

    @Transactional(readOnly = true)
    public Plan getPlanByIdWithDetails(Long planId) {
        return planRepository.findByIdWithDetails(planId)
            .orElseThrow(() -> new EntityNotFoundException("Plan not found with id: " + planId));
    }

    @Transactional
    public Plan createPlan(String name, String description) {
        log.info("Creating new plan with name: {}", name);

        var plan = Plan.builder()
            .name(name)
            .description(description)
            .build();

        return planRepository.save(plan);
    }

    @Transactional
    public PlanDetail addPlanPrice(Long planId, LocalDate effectiveDate, BigDecimal monthlyPrice) {
        log.info("Adding new price for plan: {}, effective from: {}", planId, effectiveDate);

        var plan = getPlanById(planId);

        var planDetail = PlanDetail.builder()
            .plan(plan)
            .effectiveDate(effectiveDate)
            .monthlyPrice(monthlyPrice)
            .build();

        return planDetailRepository.save(planDetail);
    }

    @Transactional(readOnly = true)
    public BigDecimal getCurrentPrice(Long planId) {
        return planDetailRepository.findActivePriceForPlan(planId, LocalDate.now())
            .map(PlanDetail::getMonthlyPrice)
            .orElseThrow(() -> new EntityNotFoundException("No active price found for plan: " + planId));
    }
}
