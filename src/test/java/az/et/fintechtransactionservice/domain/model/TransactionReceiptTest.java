package az.et.fintechtransactionservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class TransactionReceiptTest {

    @Test
    @DisplayName("Should build completed transaction receipt")
    void shouldBuildCompletedReceipt() {
        RiskDecision decision = RiskDecision.approved(12, "low risk");

        TransactionReceipt receipt = TransactionReceipt.builder()
                .transactionId(TransactionId.newId())
                .status(TransactionStatus.COMPLETED)
                .sourceWalletId(WalletId.from("wallet-001"))
                .destinationWalletId(WalletId.from("wallet-002"))
                .amount(Money.of("25.00", "AZN"))
                .fee(Money.of("0.50", "AZN"))
                .riskDecision(decision)
                .message("Transfer completed")
                .createdAt(Instant.parse("2026-05-14T10:00:00Z"))
                .build();

        assertThat(receipt.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(receipt.sourceWalletId()).contains(WalletId.from("wallet-001"));
        assertThat(receipt.destinationWalletId()).contains(WalletId.from("wallet-002"));
        assertThat(receipt.fee().amount()).isEqualByComparingTo("0.50");
        assertThat(receipt.riskDecision()).contains(decision);
    }

    @Test
    @DisplayName("Should reject missing required fields")
    void shouldRejectMissingRequiredFields() {
        assertThatThrownBy(() -> TransactionReceipt.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("transactionId");
    }
}

