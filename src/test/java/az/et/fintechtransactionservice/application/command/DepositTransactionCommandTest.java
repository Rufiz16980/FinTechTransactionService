package az.et.fintechtransactionservice.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.ClockPort;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.CustomerId;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.TransactionStatus;
import az.et.fintechtransactionservice.domain.model.TransactionType;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DepositTransactionCommandTest {

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private LedgerRepositoryPort ledgerRepository;

    @Mock
    private BalanceCachePort balanceCache;

    @Mock
    private ClockPort clock;

    @Captor
    private ArgumentCaptor<LedgerEntry> ledgerEntryCaptor;

    @Test
    @DisplayName("Should credit wallet, save ledger entry, evict cache, and return receipt")
    void shouldDepositFunds() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");
        given(walletRepository.findById(wallet.id())).willReturn(Optional.of(wallet));
        given(clock.now()).willReturn(Instant.parse("2026-05-14T10:00:00Z"));

        DepositTransactionCommand command = new DepositTransactionCommand(
                request(wallet.id()), walletRepository, ledgerRepository, balanceCache, clock);

        var receipt = command.execute();

        assertThat(wallet.balance().amount()).isEqualByComparingTo("100.00");
        assertThat(receipt.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(receipt.amount().amount()).isEqualByComparingTo("100.00");
        then(walletRepository).should(times(1)).save(wallet);
        then(balanceCache).should(times(1)).evictBalance(wallet.id());
        then(ledgerRepository).should(times(1)).save(ledgerEntryCaptor.capture());
        assertThat(ledgerEntryCaptor.getValue().type()).isEqualTo(TransactionType.DEPOSIT);
    }

    @Test
    @DisplayName("Should throw when wallet is missing")
    void shouldThrowWhenWalletMissing() {
        WalletId walletId = WalletId.from("missing-wallet");
        given(walletRepository.findById(walletId)).willReturn(Optional.empty());

        DepositTransactionCommand command = new DepositTransactionCommand(
                request(walletId), walletRepository, ledgerRepository, balanceCache, clock);

        assertThatThrownBy(command::execute)
                .isInstanceOf(WalletNotFoundException.class)
                .hasMessageContaining("missing-wallet");
    }

    private static DepositFundsRequest request(WalletId walletId) {
        return new DepositFundsRequest(walletId.value(), new BigDecimal("100.00"), "AZN", "Initial deposit");
    }
}

