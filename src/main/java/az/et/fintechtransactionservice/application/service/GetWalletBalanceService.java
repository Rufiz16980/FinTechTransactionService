package az.et.fintechtransactionservice.application.service;

import az.et.fintechtransactionservice.application.port.in.GetWalletBalanceUseCase;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class GetWalletBalanceService implements GetWalletBalanceUseCase {

    private static final Logger log = LoggerFactory.getLogger(GetWalletBalanceService.class);

    private final WalletRepositoryPort walletRepository;
    private final BalanceCachePort balanceCache;

    public GetWalletBalanceService(WalletRepositoryPort walletRepository, BalanceCachePort balanceCache) {
        this.walletRepository = walletRepository;
        this.balanceCache = balanceCache;
    }

    @Override
    public Money getBalance(String walletIdValue) {
        WalletId walletId = WalletId.from(walletIdValue);
        return balanceCache.getBalance(walletId)
                .map(balance -> {
                    log.debug("Balance cache hit: walletId={}", walletId.value());
                    return balance;
                })
                .orElseGet(() -> loadAndCacheBalance(walletId));
    }

    private Money loadAndCacheBalance(WalletId walletId) {
        log.debug("Balance cache miss: walletId={}", walletId.value());
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.value()));
        Money balance = wallet.balance();
        balanceCache.putBalance(walletId, balance);
        return balance;
    }
}

