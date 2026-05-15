package az.et.fintechtransactionservice.application.command;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.ClockPort;
import az.et.fintechtransactionservice.application.port.out.FraudCheckPort;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.TransactionRiskAssessment;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.application.strategy.FeeCalculationStrategy;
import az.et.fintechtransactionservice.application.strategy.FeeStrategyFactory;
import az.et.fintechtransactionservice.domain.exception.SameWalletTransferException;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.LedgerEntryId;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.RiskDecision;
import az.et.fintechtransactionservice.domain.model.RiskDecisionStatus;
import az.et.fintechtransactionservice.domain.model.TransactionId;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;
import az.et.fintechtransactionservice.domain.model.TransactionStatus;
import az.et.fintechtransactionservice.domain.model.TransactionType;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TransferTransactionCommand implements TransactionCommand {

    private static final Logger log = LoggerFactory.getLogger(TransferTransactionCommand.class);

    private final TransferFundsRequest request;
    private final WalletRepositoryPort walletRepository;
    private final LedgerRepositoryPort ledgerRepository;
    private final BalanceCachePort balanceCache;
    private final FraudCheckPort fraudCheck;
    private final FeeStrategyFactory feeStrategyFactory;
    private final ClockPort clock;

    public TransferTransactionCommand(
            TransferFundsRequest request,
            WalletRepositoryPort walletRepository,
            LedgerRepositoryPort ledgerRepository,
            BalanceCachePort balanceCache,
            FraudCheckPort fraudCheck,
            FeeStrategyFactory feeStrategyFactory,
            ClockPort clock) {
        this.request = request;
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
        this.balanceCache = balanceCache;
        this.fraudCheck = fraudCheck;
        this.feeStrategyFactory = feeStrategyFactory;
        this.clock = clock;
    }

    @Override
    public TransactionReceipt execute() {
        WalletId fromWalletId = WalletId.from(request.fromWalletId());
        WalletId toWalletId = WalletId.from(request.toWalletId());
        if (fromWalletId.equals(toWalletId)) {
            throw new SameWalletTransferException("Cannot transfer money to the same wallet");
        }

        Wallet sourceWallet = findWallet(fromWalletId);
        Wallet destinationWallet = findWallet(toWalletId);
        Money amount = Money.of(request.amount(), request.currency());
        TransactionId transactionId = TransactionId.newId();
        RiskDecision riskDecision = fraudCheck.assess(
                new TransactionRiskAssessment(fromWalletId, toWalletId, amount, request.description()));

        if (!riskDecision.isApproved()) {
            return rejectedReceipt(transactionId, fromWalletId, toWalletId, amount, riskDecision);
        }

        FeeCalculationStrategy feeStrategy = feeStrategyFactory.select(request);
        Money fee = feeStrategy.calculate(amount);
        Money sourceDebitAmount = amount.plus(fee);

        sourceWallet.debit(sourceDebitAmount);
        destinationWallet.credit(amount);
        walletRepository.save(sourceWallet);
        walletRepository.save(destinationWallet);

        saveTransferLedgerEntries(transactionId, sourceWallet, destinationWallet, amount, fee);
        balanceCache.evictBalance(sourceWallet.id());
        balanceCache.evictBalance(destinationWallet.id());

        log.info("Transfer completed: transactionId={}, fromWallet={}, toWallet={}, amount={}, fee={}",
                transactionId.value(), fromWalletId.value(), toWalletId.value(), amount.amount(), fee.amount());
        return TransactionReceipt.builder()
                .transactionId(transactionId)
                .status(TransactionStatus.COMPLETED)
                .sourceWalletId(sourceWallet.id())
                .destinationWalletId(destinationWallet.id())
                .amount(amount)
                .fee(fee)
                .riskDecision(riskDecision)
                .message("Transfer completed")
                .createdAt(clock.now())
                .build();
    }

    private Wallet findWallet(WalletId walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.value()));
    }

    private TransactionReceipt rejectedReceipt(
            TransactionId transactionId,
            WalletId fromWalletId,
            WalletId toWalletId,
            Money amount,
            RiskDecision riskDecision) {
        TransactionStatus status = riskDecision.status() == RiskDecisionStatus.REJECTED
                ? TransactionStatus.REJECTED
                : TransactionStatus.REVIEW_REQUIRED;
        log.warn("Transfer not completed: transactionId={}, riskStatus={}, reason={}",
                transactionId.value(), riskDecision.status(), riskDecision.reason());
        return TransactionReceipt.builder()
                .transactionId(transactionId)
                .status(status)
                .sourceWalletId(fromWalletId)
                .destinationWalletId(toWalletId)
                .amount(amount)
                .fee(Money.zero(amount.currency()))
                .riskDecision(riskDecision)
                .message(riskDecision.reason())
                .createdAt(clock.now())
                .build();
    }

    private void saveTransferLedgerEntries(
            TransactionId transactionId,
            Wallet sourceWallet,
            Wallet destinationWallet,
            Money amount,
            Money fee) {
        ledgerRepository.save(entry(transactionId, sourceWallet, TransactionType.TRANSFER_DEBIT, amount));
        ledgerRepository.save(entry(transactionId, destinationWallet, TransactionType.TRANSFER_CREDIT, amount));
        if (fee.isPositive()) {
            ledgerRepository.save(entry(transactionId, sourceWallet, TransactionType.FEE, fee));
        }
    }

    private LedgerEntry entry(TransactionId transactionId, Wallet wallet, TransactionType type, Money amount) {
        return LedgerEntry.builder()
                .entryId(LedgerEntryId.newId())
                .transactionId(transactionId)
                .walletId(wallet.id())
                .type(type)
                .amount(amount)
                .resultingBalance(wallet.balance())
                .createdAt(clock.now())
                .description(request.description())
                .build();
    }
}

