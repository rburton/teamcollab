package ai.teamcollab.server.exception;

/**
 * Exception thrown when a company has exceeded its monthly spending limit for LLM API calls.
 */
public class MonthlyLimitExceededException extends RuntimeException {

    private final Long companyId;
    private final Double limit;
    private final Double currentSpending;

    /**
     * Constructs a new MonthlyLimitExceededException with the specified detail message.
     *
     * @param message the detail message
     * @param companyId the ID of the company that exceeded its limit
     * @param limit the monthly spending limit
     * @param currentSpending the current spending amount
     */
    public MonthlyLimitExceededException(String message, Long companyId, Double limit, Double currentSpending) {
        super(message);
        this.companyId = companyId;
        this.limit = limit;
        this.currentSpending = currentSpending;
    }

    /**
     * Returns the ID of the company that exceeded its limit.
     *
     * @return the company ID
     */
    public Long getCompanyId() {
        return companyId;
    }

    /**
     * Returns the monthly spending limit.
     *
     * @return the limit
     */
    public Double getLimit() {
        return limit;
    }

    /**
     * Returns the current spending amount.
     *
     * @return the current spending
     */
    public Double getCurrentSpending() {
        return currentSpending;
    }
}