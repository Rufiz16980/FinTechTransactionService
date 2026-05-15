package az.et.fintechtransactionservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LedgerEntryTest {

    @Test
    @DisplayName("Should build ledger entry")
    void shouldBuildLedgerEntry() {
        LedgerEntry entry = LedgerEntry.builder()
                .entryId(LedgerEntryId.newId())
                .transactionId(TransactionId.newId())
                .walletId(WalletId.from("wallet-001"))
                .type(TransactionType.DEPOSIT)
                .amount(Money.of("50.00", "AZN"))
                .resultingBalance(Money.of("50.00", "AZN"))
                .createdAt(Instant.parse("2026-05-14T10:00:00Z"))
                .description("Initial deposit")
                .build();

        assertThat(entry.walletId()).isEqualTo(WalletId.from("wallet-001"));
        assertThat(entry.type()).isEqualTo(TransactionType.DEPOSIT);
        assertThat(entry.amount().amount()).isEqualByComparingTo("50.00");
        assertThat(entry.description()).isEqualTo("Initial deposit");
    }

    @Test
    @DisplayName("Should reject missing required fields")
    void shouldRejectMissingRequiredFields() {
        assertThatThrownBy(() -> LedgerEntry.builder().build())
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("entryId");
    }
}

