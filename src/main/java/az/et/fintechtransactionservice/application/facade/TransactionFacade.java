package az.et.fintechtransactionservice.application.facade;

import az.et.fintechtransactionservice.application.factory.TransactionCommandFactory;
import az.et.fintechtransactionservice.application.model.CreateWalletRequest;
import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.in.CreateWalletUseCase;
import az.et.fintechtransactionservice.application.port.in.DepositFundsUseCase;
import az.et.fintechtransactionservice.application.port.in.GetTransactionHistoryUseCase;
import az.et.fintechtransactionservice.application.port.in.TransferFundsUseCase;
import az.et.fintechtransactionservice.application.port.out.LedgerRepositoryPort;
import az.et.fintechtransactionservice.application.port.out.WalletRepositoryPort;
import az.et.fintechtransactionservice.domain.exception.WalletNotFoundException;
import az.et.fintechtransactionservice.domain.model.CustomerId;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class TransactionFacade implements CreateWalletUseCase, DepositFundsUseCase, TransferFundsUseCase,
        GetTransactionHistoryUseCase {

    private static final Logger log = LoggerFactory.getLogger(TransactionFacade.class);

    private final WalletRepositoryPort walletRepository;
    private final LedgerRepositoryPort ledgerRepository;
    private final TransactionCommandFactory commandFactory;

    public TransactionFacade(
            WalletRepositoryPort walletRepository,
            LedgerRepositoryPort ledgerRepository,
            TransactionCommandFactory commandFactory) {
        this.walletRepository = walletRepository;
        this.ledgerRepository = ledgerRepository;
        this.commandFactory = commandFactory;
    }

    @Override
    public Wallet createWallet(CreateWalletRequest request) {
        Wallet wallet = Wallet.open(new CustomerId(request.customerId()), request.currency());
        walletRepository.save(wallet);
        log.info("Wallet created: walletId={}, customerId={}, currency={}",
                wallet.id().value(), wallet.customerId().value(), wallet.balance().currency());
        return wallet;
    }

    @Override
    public TransactionReceipt deposit(DepositFundsRequest request) {
        return commandFactory.createDeposit(request).execute();
    }

    @Override
    public TransactionReceipt transfer(TransferFundsRequest request) {
        return commandFactory.createTransfer(request).execute();
    }

    @Override
    public List<LedgerEntry> getLedger(String walletIdValue) {
        WalletId walletId = WalletId.from(walletIdValue);
        walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException(walletId.value()));
        return ledgerRepository.findByWalletId(walletId);
    }
}

