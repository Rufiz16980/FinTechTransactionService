package az.et.fintechtransactionservice.application.factory;

import az.et.fintechtransactionservice.application.command.DepositTransactionCommand;
import az.et.fintechtransactionservice.application.command.TransactionCommand;
import az.et.fintechtransactionservice.application.command.TransferTransactionCommand;
import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.ClockPort;
import az.et.fintechtransactionservice.application.port.out.FraudCheckPort;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.application.strategy.FeeStrategyFactory;
import org.springframework.stereotype.Component;

@Component
public class TransactionCommandFactory {

    private final WalletRepositoryPort walletRepository;
    private final LedgerRepositoryPort ledgerRepository;
    private final BalanceCachePort balanceCache;
    private final FraudCheckPort fraudCheck;
    private final FeeStrategyFactory feeStrategyFactory;
    private final ClockPort clock;

    public TransactionCommandFactory(
            WalletRepositoryPort walletRepository,
            LedgerRepositoryPort ledgerRepository,
            BalanceCachePort balanceCache,
            FraudCheckPort fraudCheck,
            FeeStrategyFactory feeStrategyFactory,
            ClockPort clock) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
        this.balanceCache = balanceCache;
        this.fraudCheck = fraudCheck;
        this.feeStrategyFactory = feeStrategyFactory;
        this.clock = clock;
    }

    public TransactionCommand createDeposit(DepositFundsRequest request) {
        return new DepositTransactionCommand(request, walletRepository, ledgerRepository, balanceCache, clock);
    }

    public TransactionCommand createTransfer(TransferFundsRequest request) {
        return new TransferTransactionCommand(
                request, walletRepository, ledgerRepository, balanceCache, fraudCheck, feeStrategyFactory, clock);
    }
}

