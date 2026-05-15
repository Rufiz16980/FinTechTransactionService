package az.et.fintechtransactionservice.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.CustomerId;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GetWalletBalanceServiceTest {

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private BalanceCachePort balanceCache;

    @InjectMocks
    private GetWalletBalanceService service;

    @Test
    @DisplayName("Should return cached balance on cache hit")
    void shouldReturnCachedBalance() {
        WalletId walletId = WalletId.from("wallet-001");
        Money cachedBalance = Money.of("42.00", "AZN");
        given(balanceCache.getBalance(walletId)).willReturn(Optional.of(cachedBalance));

        Money result = service.getBalance(walletId.value());

        assertThat(result).isEqualTo(cachedBalance);
        then(walletRepository).should(never()).findById(walletId);
    }

    @Test
    @DisplayName("Should load balance from repository and cache it on cache miss")
    void shouldLoadAndCacheBalanceOnMiss() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");
        wallet.credit(Money.of("50.00", "AZN"));
        given(balanceCache.getBalance(wallet.id())).willReturn(Optional.empty());
        given(walletRepository.findById(wallet.id())).willReturn(Optional.of(wallet));

        Money result = service.getBalance(wallet.id().value());

        assertThat(result.amount()).isEqualByComparingTo("50.00");
        then(balanceCache).should(times(1)).putBalance(wallet.id(), wallet.balance());
    }

    @Test
    @DisplayName("Should throw when wallet is missing on cache miss")
    void shouldThrowWhenWalletMissing() {
        WalletId walletId = WalletId.from("missing-wallet");
        given(balanceCache.getBalance(walletId)).willReturn(Optional.empty());
        given(walletRepository.findById(walletId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> service.getBalance(walletId.value()))
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("missing-wallet");
    }
}

