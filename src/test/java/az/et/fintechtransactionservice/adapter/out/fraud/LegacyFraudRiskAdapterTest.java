package az.et.fintechtransactionservice.adapter.out.fraud;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import az.et.fintechtransactionservice.adapter.out.fraud.legacy.LegacyFraudRiskClient;
import az.et.fintechtransactionservice.application.port.out.TransactionRiskAssessment;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.RiskDecisionStatus;
import az.et.fintechtransactionservice.domain.model.WalletId;
import io.github.resilience4j.circuitbreaker.CircuitBreaker;
import io.github.resilience4j.circuitbreaker.CircuitBreakerConfig;
import io.github.resilience4j.circuitbreaker.CircuitBreakerRegistry;
import java.time.Duration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class LegacyFraudRiskAdapterTest {

    @Mock
    private LegacyFraudRiskClient legacyClient;

    @Captor
    private ArgumentCaptor<String> payloadCaptor;

    @Test
    @DisplayName("Should translate clean assessment to legacy payload and map approved response")
    void shouldMapApprovedResponse() {
        given(legacyClient.chk(org.mockito.ArgumentMatchers.anyString())).willReturn("OK|score=12");
        LegacyFraudRiskAdapter adapter = new LegacyFraudRiskAdapter(legacyClient, CircuitBreakerRegistry.ofDefaults());

        var decision = adapter.assess(assessment("25.00", "Dinner"));

        assertThat(decision.status()).isEqualTo(RiskDecisionStatus.APPROVED);
        assertThat(decision.score()).isEqualTo(12);
        then(legacyClient).should(times(1)).chk(payloadCaptor.capture());
        assertThat(payloadCaptor.getValue())
                .contains("FROM=wallet-001")
                .contains("TO=wallet-002")
                .contains("AMT=25.00")
                .contains("CUR=AZN")
                .contains("DESC=Dinner");
    }

    @Test
    @DisplayName("Should map review response")
    void shouldMapReviewResponse() {
        given(legacyClient.chk(org.mockito.ArgumentMatchers.anyString())).willReturn("REVIEW|score=70");
        LegacyFraudRiskAdapter adapter = new LegacyFraudRiskAdapter(legacyClient, CircuitBreakerRegistry.ofDefaults());

        var decision = adapter.assess(assessment("1000.00", "Invoice"));

        assertThat(decision.status()).isEqualTo(RiskDecisionStatus.REVIEW_REQUIRED);
        assertThat(decision.score()).isEqualTo(70);
    }

    @Test
    @DisplayName("Should map block response")
    void shouldMapBlockResponse() {
        given(legacyClient.chk(org.mockito.ArgumentMatchers.anyString())).willReturn("BLOCK|score=95");
        LegacyFraudRiskAdapter adapter = new LegacyFraudRiskAdapter(legacyClient, CircuitBreakerRegistry.ofDefaults());

        var decision = adapter.assess(assessment("5000.00", "Large transfer"));

        assertThat(decision.status()).isEqualTo(RiskDecisionStatus.REJECTED);
        assertThat(decision.score()).isEqualTo(95);
    }

    @Test
    @DisplayName("Should fallback to review required when provider throws")
    void shouldFallbackWhenProviderThrows() {
        given(legacyClient.chk(org.mockito.ArgumentMatchers.anyString()))
                .willThrow(new FraudProviderUnavailableException("down"));
        LegacyFraudRiskAdapter adapter = new LegacyFraudRiskAdapter(legacyClient, CircuitBreakerRegistry.ofDefaults());

        var decision = adapter.assess(assessment("25.00", "FAIL_FRAUD"));

        assertThat(decision.status()).isEqualTo(RiskDecisionStatus.REVIEW_REQUIRED);
        assertThat(decision.reason()).contains("manual review");
    }

    @Test
    @DisplayName("Should open circuit after repeated fraud provider failures")
    void shouldOpenCircuitAfterFailures() {
        given(legacyClient.chk(org.mockito.ArgumentMatchers.anyString()))
                .willThrow(new FraudProviderUnavailableException("down"));
        CircuitBreakerConfig config = CircuitBreakerConfig.custom()
                .slidingWindowSize(2)
                .minimumNumberOfCalls(2)
                .failureRateThreshold(50)
                .waitDurationInOpenState(Duration.ofSeconds(30))
                .build();
        CircuitBreakerRegistry registry = CircuitBreakerRegistry.of(config);
        LegacyFraudRiskAdapter adapter = new LegacyFraudRiskAdapter(legacyClient, registry);

        adapter.assess(assessment("25.00", "FAIL_FRAUD"));
        adapter.assess(assessment("25.00", "FAIL_FRAUD"));
        adapter.assess(assessment("25.00", "FAIL_FRAUD"));

        assertThat(registry.circuitBreaker("legacyFraudRiskClient").getState()).isEqualTo(CircuitBreaker.State.OPEN);
        then(legacyClient).should(times(2)).chk(org.mockito.ArgumentMatchers.anyString());
    }

    private static TransactionRiskAssessment assessment(String amount, String description) {
        return new TransactionRiskAssessment(
                WalletId.from("wallet-001"),
                WalletId.from("wallet-002"),
                Money.of(amount, "AZN"),
                description);
    }
}

