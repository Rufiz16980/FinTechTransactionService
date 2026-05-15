package az.et.fintechtransactionservice.application.command;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.ClockPort;
import az.et.fintechtransactionservice.application.port.out.FraudCheckPort;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.application.strategy.FeeStrategyFactory;
import az.et.fintechtransactionservice.application.strategy.FlatFeeStrategy;
import az.et.fintechtransactionservice.domain.exception.SameWalletTransferException;
import az.et.fintechtransactionservice.domain.model.CustomerId;
import az.et.fintechtransactionservice.domain.model.RiskDecision;
import az.et.fintechtransactionservice.domain.model.TransactionStatus;
import az.et.fintechtransactionservice.domain.model.Wallet;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransferTransactionCommandTest {

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private LedgerRepositoryPort ledgerRepository;

    @Mock
    private BalanceCachePort balanceCache;

    @Mock
    private FraudCheckPort fraudCheck;

    @Mock
    private ClockPort clock;

    @Test
    @DisplayName("Should transfer amount, charge fee, save ledger entries, and evict both balances")
    void shouldTransferFunds() {
        Wallet source = Wallet.open(new CustomerId("source-customer"), "AZN");
        Wallet destination = Wallet.open(new CustomerId("destination-customer"), "AZN");
        source.credit(azn("100.00"));
        given(walletRepository.findById(source.id())).willReturn(Optional.of(source));
        given(walletRepository.findById(destination.id())).willReturn(Optional.of(destination));
        given(fraudCheck.assess(org.mockito.ArgumentMatchers.any())).willReturn(RiskDecision.approved(12, "low risk"));
        given(clock.now()).willReturn(Instant.parse("2026-05-14T10:00:00Z"));

        TransferTransactionCommand command = command(source, destination, "Dinner split");

        var receipt = command.execute();

        assertThat(receipt.status()).isEqualTo(TransactionStatus.COMPLETED);
        assertThat(receipt.fee().amount()).isEqualByComparingTo("0.50");
        assertThat(source.balance().amount()).isEqualByComparingTo("74.50");
        assertThat(destination.balance().amount()).isEqualByComparingTo("25.00");
        then(walletRepository).should(times(1)).save(source);
        then(walletRepository).should(times(1)).save(destination);
        then(ledgerRepository).should(times(3)).save(org.mockito.ArgumentMatchers.any());
        then(balanceCache).should(times(1)).evictBalance(source.id());
        then(balanceCache).should(times(1)).evictBalance(destination.id());
    }

    @Test
    @DisplayName("Should not move money when fraud decision requires review")
    void shouldStopWhenReviewRequired() {
        Wallet source = Wallet.open(new CustomerId("source-customer"), "AZN");
        Wallet destination = Wallet.open(new CustomerId("destination-customer"), "AZN");
        source.credit(azn("100.00"));
        given(walletRepository.findById(source.id())).willReturn(Optional.of(source));
        given(walletRepository.findById(destination.id())).willReturn(Optional.of(destination));
        given(fraudCheck.assess(org.mockito.ArgumentMatchers.any()))
                .willReturn(RiskDecision.reviewRequired(80, "manual review"));
        given(clock.now()).willReturn(Instant.parse("2026-05-14T10:00:00Z"));

        TransferTransactionCommand command = command(source, destination, "Suspicious transfer");

        var receipt = command.execute();

        assertThat(receipt.status()).isEqualTo(TransactionStatus.REVIEW_REQUIRED);
        assertThat(source.balance().amount()).isEqualByComparingTo("100.00");
        assertThat(destination.balance().amount()).isEqualByComparingTo("0.00");
        then(walletRepository).should(never()).save(source);
        then(walletRepository).should(never()).save(destination);
        then(ledgerRepository).should(never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    @DisplayName("Should reject same-wallet transfer")
    void shouldRejectSameWalletTransfer() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");
        TransferFundsRequest request = new TransferFundsRequest(
                wallet.id().value(), wallet.id().value(), new BigDecimal("25.00"), "AZN", "Invalid");
        TransferTransactionCommand command = new TransferTransactionCommand(
                request,
                walletRepository,
                ledgerRepository,
                balanceCache,
                fraudCheck,
                new FeeStrategyFactory(List.of(new FlatFeeStrategy("0.50"))),
                clock);

        assertThatThrownBy(command::execute).isInstanceOf(SameWalletTransferException.class);
    }

    private TransferTransactionCommand command(Wallet source, Wallet destination, String description) {
        TransferFundsRequest request = new TransferFundsRequest(
                source.id().value(), destination.id().value(), new BigDecimal("25.00"), "AZN", description);
        return new TransferTransactionCommand(
                request,
                walletRepository,
                ledgerRepository,
                balanceCache,
                fraudCheck,
                new FeeStrategyFactory(List.of(new FlatFeeStrategy("0.50"))),
                clock);
    }

    private static az.et.fintechtransactionservice.domain.model.Money azn(String amount) {
        return az.et.fintechtransactionservice.domain.model.Money.of(amount, "AZN");
    }
}

