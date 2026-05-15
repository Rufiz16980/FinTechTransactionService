package az.et.fintechtransactionservice.adapter.out.fraud.legacy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import az.et.fintechtransactionservice.adapter.out.fraud.FraudProviderUnavailableException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LegacyFraudRiskClientTest {

    private final LegacyFraudRiskClient client = new LegacyFraudRiskClient("FAIL_FRAUD");

    @Test
    @DisplayName("Should approve low-value transfer")
    void shouldApproveLowValueTransfer() {
        String response = client.chk("FROM=w1|TO=w2|AMT=25.00|CUR=AZN|DESC=Dinner");

        assertThat(response).isEqualTo("OK|score=12");
    }

    @Test
    @DisplayName("Should require review for medium-value transfer")
    void shouldReviewMediumValueTransfer() {
        String response = client.chk("FROM=w1|TO=w2|AMT=1000.00|CUR=AZN|DESC=Invoice");

        assertThat(response).isEqualTo("REVIEW|score=70");
    }

    @Test
    @DisplayName("Should block high-value transfer")
    void shouldBlockHighValueTransfer() {
        String response = client.chk("FROM=w1|TO=w2|AMT=5000.00|CUR=AZN|DESC=Large transfer");

        assertThat(response).isEqualTo("BLOCK|score=95");
    }

    @Test
    @DisplayName("Should throw when outage keyword is present")
    void shouldThrowForOutageKeyword() {
        assertThatThrownBy(() -> client.chk("FROM=w1|TO=w2|AMT=25.00|CUR=AZN|DESC=FAIL_FRAUD"))
                .isInstanceOf(FraudProviderUnavailableException.class)
                .hasMessageContaining("unavailable");
    }
}

