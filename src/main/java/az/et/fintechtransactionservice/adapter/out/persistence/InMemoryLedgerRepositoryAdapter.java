package az.et.fintechtransactionservice.adapter.out.persistence;

import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryLedgerRepositoryAdapter implements LedgerRepositoryPort {

    private final CopyOnWriteArrayList<LedgerEntry> entries = new CopyOnWriteArrayList<>();

    @Override
    public LedgerEntry save(LedgerEntry entry) {
        Objects.requireNonNull(entry, "entry must not be null");
        entries.add(entry);
        return entry;
    }

    @Override
    public List<LedgerEntry> findByWalletId(WalletId walletId) {
        Objects.requireNonNull(walletId, "walletId must not be null");
        return entries.stream()
                .filter(entry -> entry.walletId().equals(walletId))
                .sorted(Comparator.comparing(LedgerEntry::createdAt))
                .toList();
    }
}

