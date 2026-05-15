package az.et.fintechtransactionservice.adapter.out.fraud;

import az.et.fintechtransactionservice.adapter.out.fraud.legacy.LegacyFraudRiskClient;
import az.et.fintechtransactionservice.application.port.out.FraudCheckPort;
import az.et.fintechtransactionservice.application.port.out.TransactionRiskAssessment;
import az.et.fintechtransactionservice.domain.model.RiskDecision;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.util.function.Supplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class LegacyFraudRiskAdapter implements FraudCheckPort {

    private static final Logger log = LoggerFactory.getLogger(LegacyFraudRiskAdapter.class);
    private static final String CIRCUIT_BREAKER_NAME = "legacyFraudRiskClient";

    private final LegacyFraudRiskClient legacyClient;
    private final CircuitBreaker circuitBreaker;

    public LegacyFraudRiskAdapter(LegacyFraudRiskClient legacyClient, CircuitBreakerRegistry registry) {
        this.legacyClient = legacyClient;
        this.circuitBreaker = registry.circuitBreaker(CIRCUIT_BREAKER_NAME);
    }

    @Override
    public RiskDecision assess(TransactionRiskAssessment assessment) {
        Supplier<RiskDecision> protectedCall = CircuitBreaker.decorateSupplier(
                circuitBreaker, () -> callLegacyProvider(assessment));
        try {
            RiskDecision decision = protectedCall.get();
            log.info("Fraud decision received: status={}, score={}", decision.status(), decision.score());
            return decision;
        } catch (RuntimeException exception) {
            log.warn("Fraud provider unavailable, using fallback decision: reason={}", exception.getMessage());
            return RiskDecision.reviewRequired(75, "Fraud provider unavailable; manual review required");
        }
    }

    private RiskDecision callLegacyProvider(TransactionRiskAssessment assessment) {
        String response = legacyClient.chk(toLegacyPayload(assessment));
        return fromLegacyResponse(response);
    }

    private String toLegacyPayload(TransactionRiskAssessment assessment) {
        return "FROM=" + assessment.fromWalletId().value()
                + "|TO=" + assessment.toWalletId().value()
                + "|AMT=" + assessment.amount().amount()
                + "|CUR=" + assessment.amount().currency()
                + "|DESC=" + assessment.description();
    }

    private RiskDecision fromLegacyResponse(String response) {
        int score = scoreFrom(response);
        if (response.startsWith("OK")) {
            return RiskDecision.approved(score, "Fraud check approved");
        }
        if (response.startsWith("REVIEW")) {
            return RiskDecision.reviewRequired(score, "Fraud check requires manual review");
        }
        if (response.startsWith("BLOCK")) {
            return RiskDecision.rejected(score, "Fraud check rejected transaction");
        }
        return RiskDecision.reviewRequired(75, "Malformed fraud provider response");
    }

    private int scoreFrom(String response) {
        String marker = "score=";
        int start = response.indexOf(marker);
        if (start < 0) {
            return 75;
        }
        return Integer.parseInt(response.substring(start + marker.length()));
    }
}

