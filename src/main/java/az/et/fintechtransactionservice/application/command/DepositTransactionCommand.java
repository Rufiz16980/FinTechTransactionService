package az.et.fintechtransactionservice.application.command;

import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.port.out.BalanceCachePort;
import az.et.fintechtransactionservice.application.port.out.ClockPort;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.LedgerEntryId;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.TransactionId;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;
import az.et.fintechtransactionservice.domain.model.TransactionStatus;
import az.et.fintechtransactionservice.domain.model.TransactionType;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DepositTransactionCommand implements TransactionCommand {

    private static final Logger log = LoggerFactory.getLogger(DepositTransactionCommand.class);

    private final DepositFundsRequest request;
    private final WalletRepositoryPort walletRepository;
    private final LedgerRepositoryPort ledgerRepository;
    private final BalanceCachePort balanceCache;
    private final ClockPort clock;

    public DepositTransactionCommand(
            DepositFundsRequest request,
            WalletRepositoryPort walletRepository,
            LedgerRepositoryPort ledgerRepository,
            BalanceCachePort balanceCache,
            ClockPort clock) {
        this.request = request;
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
        this.balanceCache = balanceCache;
        this.clock = clock;
    }

    @Override
    public TransactionReceipt execute() {
        WalletId walletId = WalletId.from(request.walletId());
        Money amount = Money.of(request.amount(), request.currency());
        Wallet wallet = walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.value()));
        TransactionId transactionId = TransactionId.newId();

        wallet.credit(amount);
        walletRepository.save(wallet);
        ledgerRepository.save(LedgerEntry.builder()
                .entryId(LedgerEntryId.newId())
                .transactionId(transactionId)
                .walletId(wallet.id())
                .type(TransactionType.DEPOSIT)
                .amount(amount)
                .resultingBalance(wallet.balance())
                .createdAt(clock.now())
                .description(request.description())
                .build());
        balanceCache.evictBalance(wallet.id());

        log.info("Deposit completed: transactionId={}, walletId={}, amount={}",
                transactionId.value(), wallet.id().value(), amount.amount());
        return TransactionReceipt.builder()
                .transactionId(transactionId)
                .status(TransactionStatus.COMPLETED)
                .destinationWalletId(wallet.id())
                .amount(amount)
                .fee(Money.zero(amount.currency()))
                .message("Deposit completed")
                .createdAt(clock.now())
                .build();
    }
}

