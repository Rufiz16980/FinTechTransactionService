package az.et.fintechtransactionservice.domain.model;

import java.util.Objects;

public record RiskDecision(RiskDecisionStatus status, int score, String reason) {

    public RiskDecision {
        Objects.requireNonNull(status, "status must not be null");
        reason = Objects.requireNonNullElse(reason, "");
        if (score < 0 || score > 100) {
            throw new IllegalArgumentException("Risk score must be between 0 and 100");
        }
    }

    public static RiskDecision approved(int score, String reason) {
        return new RiskDecision(RiskDecisionStatus.APPROVED, score, reason);
    }

    public static RiskDecision reviewRequired(int score, String reason) {
        return new RiskDecision(RiskDecisionStatus.REVIEW_REQUIRED, score, reason);
    }

    public static RiskDecision rejected(int score, String reason) {
        return new RiskDecision(RiskDecisionStatus.REJECTED, score, reason);
    }

    public boolean isApproved() {
        return status == RiskDecisionStatus.APPROVED;
    }
}

