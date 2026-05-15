package az.et.fintechtransactionservice.application.port.in;

import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import java.util.List;

public interface GetTransactionHistoryUseCase {

    List<LedgerEntry> getLedger(String walletId);
}

