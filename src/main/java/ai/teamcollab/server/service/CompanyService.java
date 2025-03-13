package ai.teamcollab.server.service;

import ai.teamcollab.server.domain.Company;
import ai.teamcollab.server.domain.LlmModel;
import ai.teamcollab.server.domain.User;
import ai.teamcollab.server.repository.CompanyRepository;
import ai.teamcollab.server.repository.LlmModelRepository;
import ai.teamcollab.server.repository.PlanDetailRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final PlanDetailRepository planDetailRepository;
    private final LlmModelRepository llmModelRepository;

    public CompanyService(CompanyRepository companyRepository, PlanDetailRepository planDetailRepository, LlmModelRepository llmModelRepository) {
        this.companyRepository = companyRepository;
        this.planDetailRepository = planDetailRepository;
        this.llmModelRepository = llmModelRepository;
    }

    @Transactional
    public Company createCompany(Company company) {
        planDetailRepository.findFreePlan()
                .ifPresent(company::addSubscription);

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
    public Company updateCompanyLlmModel(Long companyId, String modelId) {
        Company company = getCompanyById(companyId);

        if (modelId == null || modelId.isEmpty()) {
            company.setLlmModel(null);
        } else {
            try {
                Long modelIdLong = Long.parseLong(modelId);
                LlmModel llmModel = llmModelRepository.findById(modelIdLong)
                        .orElseThrow(() -> new IllegalArgumentException("LLM model not found with id: " + modelId));
                company.setLlmModel(llmModel);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid LLM model id: " + modelId);
            }
        }

        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompanyMonthlySpendingLimit(Long companyId, BigDecimal monthlySpendingLimit) {
        Company company = getCompanyById(companyId);
        company.setMonthlySpendingLimit(monthlySpendingLimit);
        return companyRepository.save(company);
    }

    @Transactional
    public Company updateCompanyName(Long companyId, String name) {
        Company company = getCompanyById(companyId);
        company.setName(name);
        return companyRepository.save(company);
    }

}
