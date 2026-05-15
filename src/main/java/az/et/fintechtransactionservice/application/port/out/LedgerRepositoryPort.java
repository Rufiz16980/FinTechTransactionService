package az.et.fintechtransactionservice.application.port.out;

import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.List;

public interface LedgerRepositoryPort {

    LedgerEntry save(LedgerEntry entry);

    List<LedgerEntry> findByWalletId(WalletId walletId);
}

