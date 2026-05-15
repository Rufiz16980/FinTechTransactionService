package az.et.fintechtransactionservice.application.factory;

import static org.assertj.core.api.Assertions.assertThat;

import az.et.fintechtransactionservice.application.command.DepositTransactionCommand;
import az.et.fintechtransactionservice.application.command.TransferTransactionCommand;
import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.ClockPort;
import az.et.fintechtransactionservice.application.port.out.FraudCheckPort;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.application.strategy.FeeStrategyFactory;
import az.et.fintechtransactionservice.application.strategy.FlatFeeStrategy;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TransactionCommandFactoryTest {

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
    @DisplayName("Should create deposit command")
    void shouldCreateDepositCommand() {
        TransactionCommandFactory factory = factory();

        var command = factory.createDeposit(new DepositFundsRequest("wallet-001", BigDecimal.ONE, "AZN", "Deposit"));

        assertThat(command).isInstanceOf(DepositTransactionCommand.class);
    }

    @Test
    @DisplayName("Should create transfer command")
    void shouldCreateTransferCommand() {
        TransactionCommandFactory factory = factory();
        TransferFundsRequest request = new TransferFundsRequest(
                "wallet-001", "wallet-002", BigDecimal.ONE, "AZN", "Transfer");

        var command = factory.createTransfer(request);

        assertThat(command).isInstanceOf(TransferTransactionCommand.class);
    }

    private TransactionCommandFactory factory() {
        return new TransactionCommandFactory(
                walletRepository,
                ledgerRepository,
                balanceCache,
                fraudCheck,
                new FeeStrategyFactory(List.of(new FlatFeeStrategy("0.50"))),
                clock);
    }
}

