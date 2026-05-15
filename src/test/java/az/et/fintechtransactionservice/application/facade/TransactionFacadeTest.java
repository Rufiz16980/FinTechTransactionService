package az.et.fintechtransactionservice.application.facade;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import az.et.fintechtransactionservice.application.command.TransactionCommand;
import az.et.fintechtransactionservice.application.factory.TransactionCommandFactory;
import az.et.fintechtransactionservice.application.model.CreateWalletRequest;
import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.TransactionId;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;
import az.et.fintechtransactionservice.domain.model.TransactionStatus;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
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
class TransactionFacadeTest {

    @Mock
    private WalletRepositoryPort walletRepository;

    @Mock
    private LedgerRepositoryPort ledgerRepository;

    @Mock
    private TransactionCommandFactory commandFactory;

    @Mock
    private TransactionCommand command;

    @Test
    @DisplayName("Should create wallet through repository")
    void shouldCreateWallet() {
        TransactionFacade facade = facade();

        Wallet wallet = facade.createWallet(new CreateWalletRequest("customer-001", "azn"));

        assertThat(wallet.customerId().value()).isEqualTo("customer-001");
        assertThat(wallet.balance().currency()).isEqualTo("AZN");
        then(walletRepository).should(times(1)).save(wallet);
    }

    @Test
    @DisplayName("Should delegate deposit to factory-created command")
    void shouldDelegateDeposit() {
        DepositFundsRequest request = new DepositFundsRequest("wallet-001", BigDecimal.ONE, "AZN", "Deposit");
        TransactionReceipt receipt = receipt();
        given(commandFactory.createDeposit(request)).willReturn(command);
        given(command.execute()).willReturn(receipt);

        assertThat(facade().deposit(request)).isSameAs(receipt);
    }

    @Test
    @DisplayName("Should delegate transfer to factory-created command")
    void shouldDelegateTransfer() {
        TransferFundsRequest request = new TransferFundsRequest(
                "wallet-001", "wallet-002", BigDecimal.ONE, "AZN", "Transfer");
        TransactionReceipt receipt = receipt();
        given(commandFactory.createTransfer(request)).willReturn(command);
        given(command.execute()).willReturn(receipt);

        assertThat(facade().transfer(request)).isSameAs(receipt);
    }

    @Test
    @DisplayName("Should return ledger when wallet exists")
    void shouldReturnLedger() {
        Wallet wallet = Wallet.open(new az.et.fintechtransactionservice.domain.model.CustomerId("customer-001"), "AZN");
        given(walletRepository.findById(wallet.id())).willReturn(Optional.of(wallet));
        given(ledgerRepository.findByWalletId(wallet.id())).willReturn(List.of());

        assertThat(facade().getLedger(wallet.id().value())).isEmpty();
    }

    @Test
    @DisplayName("Should throw when ledger wallet is missing")
    void shouldThrowForMissingLedgerWallet() {
        WalletId walletId = WalletId.from("missing-wallet");
        given(walletRepository.findById(walletId)).willReturn(Optional.empty());

        assertThatThrownBy(() -> facade().getLedger(walletId.value()))
                .isInstanceOf(WalletNotFoundException.class);
    }

    private TransactionFacade facade() {
        return new TransactionFacade(walletRepository, ledgerRepository, commandFactory);
    }

    private static TransactionReceipt receipt() {
        return TransactionReceipt.builder()
                .transactionId(TransactionId.newId())
                .status(TransactionStatus.COMPLETED)
                .amount(Money.of("1.00", "AZN"))
                .fee(Money.zero("AZN"))
                .createdAt(Instant.parse("2026-05-14T10:00:00Z"))
                .build();
    }
}
