package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.Subscription;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.CompanyRepository;
import ai.teamcollab.server.repository.PlanDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PlanDetailRepository planDetailRepository;

    public CompanyService(CompanyRepository companyRepository, PlanDetailRepository planDetailRepository) {
        this.companyRepository = companyRepository;
        this.planDetailRepository = planDetailRepository;
    }

    @Transactional
    public Company createCompany(Company company) {
        planDetailRepository.findFreePlan()
                .ifPresent(planDetails -> {

                    company.addSubscription(planDetails);
                });

        return companyRepository.save(company);
    }

    @Transactional
    public Company addUserToCompany(Company company, User user) {
        company.addUser(user);
        return companyRepository.save(company);
    }

    public Company getCompanyById(Long id) {
        return companyRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
    }

    public Iterable<Company> getAllCompanies() {
        return companyRepository.findAll();
    }

    @Transactional
    public Company updateCompanyLlmModel(Long companyId, String llmModel) {
        Company company = getCompanyById(companyId);
        company.setLlmModel(llmModel);
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompanyMonthlySpendingLimit(Long companyId, BigDecimal monthlySpendingLimit) {
        Company company = getCompanyById(companyId);
        company.setMonthlySpendingLimit(monthlySpendingLimit);
        return companyRepository.save(company);
    }

}
