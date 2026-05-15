package az.et.fintechtransactionservice.adapter.out.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.LedgerEntryId;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.TransactionId;
import az.et.fintechtransactionservice.domain.model.TransactionType;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class InMemoryLedgerRepositoryAdapterTest {

    private final InMemoryLedgerRepositoryAdapter repository = new InMemoryLedgerRepositoryAdapter();

    @Test
    @DisplayName("Should save and find ledger entries by wallet id in chronological order")
    void shouldFindLedgerEntriesByWalletId() {
        WalletId walletId = WalletId.from("wallet-001");
        LedgerEntry second = entry(walletId, "2026-05-14T10:00:02Z");
        LedgerEntry first = entry(walletId, "2026-05-14T10:00:01Z");
        LedgerEntry otherWallet = entry(WalletId.from("wallet-002"), "2026-05-14T10:00:03Z");

        repository.save(second);
        repository.save(first);
        repository.save(otherWallet);

        assertThat(repository.findByWalletId(walletId))
                .containsExactly(first, second);
    }

    private static LedgerEntry entry(WalletId walletId, String createdAt) {
        return LedgerEntry.builder()
                .entryId(LedgerEntryId.newId())
                .transactionId(TransactionId.newId())
                .walletId(walletId)
                .type(TransactionType.DEPOSIT)
                .amount(Money.of("10.00", "AZN"))
                .resultingBalance(Money.of("10.00", "AZN"))
                .createdAt(Instant.parse(createdAt))
                .description("Test")
                .build();
    }
}

