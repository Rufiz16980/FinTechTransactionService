package az.et.fintechtransactionservice.application.port.out;

import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.Optional;

public interface BalanceCachePort {

    Optional<Money> getBalance(WalletId walletId);

    void putBalance(WalletId walletId, Money balance);

    void evictBalance(WalletId walletId);
}

