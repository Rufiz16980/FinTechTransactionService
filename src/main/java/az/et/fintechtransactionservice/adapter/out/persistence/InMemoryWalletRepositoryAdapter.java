package az.et.fintechtransactionservice.adapter.out.persistence;

import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.springframework.stereotype.Repository;

@Repository
public class InMemoryWalletRepositoryAdapter implements WalletRepositoryPort {

    private final ConcurrentMap<WalletId, Wallet> wallets = new ConcurrentHashMap<>();

    @Override
    public Wallet save(Wallet wallet) {
        Objects.requireNonNull(wallet, "wallet must not be null");
        wallets.put(wallet.id(), wallet);
        return wallet;
    }

    @Override
    public Optional<Wallet> findById(WalletId walletId) {
        Objects.requireNonNull(walletId, "walletId must not be null");
        return Optional.ofNullable(wallets.get(walletId));
    }
}

