package az.et.fintechtransactionservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RiskDecisionTest {

    @Test
    @DisplayName("Should create approved decision")
    void shouldCreateApprovedDecision() {
        RiskDecision decision = RiskDecision.approved(10, "low risk");

        assertThat(decision.isApproved()).isTrue();
        assertThat(decision.status()).isEqualTo(RiskDecisionStatus.APPROVED);
    }

    @Test
    @DisplayName("Should reject out-of-range score")
    void shouldRejectOutOfRangeScore() {
        assertThatThrownBy(() -> RiskDecision.approved(101, "invalid"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("between 0 and 100");
    }
}

