package az.et.fintechtransactionservice.adapter.in.web;

import az.et.fintechtransactionservice.adapter.in.web.dto.BalanceResponse;
import az.et.fintechtransactionservice.adapter.in.web.dto.CreateWalletHttpRequest;
import az.et.fintechtransactionservice.adapter.in.web.dto.LedgerEntryResponse;
import az.et.fintechtransactionservice.adapter.in.web.dto.WalletResponse;
import az.et.fintechtransactionservice.application.model.CreateWalletRequest;
import az.et.fintechtransactionservice.application.port.in.CreateWalletUseCase;
import az.et.fintechtransactionservice.application.port.in.GetTransactionHistoryUseCase;
import az.et.fintechtransactionservice.application.port.in.GetWalletBalanceUseCase;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/wallets")
public class WalletController {

    private final CreateWalletUseCase createWalletUseCase;
    private final GetWalletBalanceUseCase getWalletBalanceUseCase;
    private final GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    public WalletController(
            CreateWalletUseCase createWalletUseCase,
            GetWalletBalanceUseCase getWalletBalanceUseCase,
            GetTransactionHistoryUseCase getTransactionHistoryUseCase) {
        this.createWalletUseCase = createWalletUseCase;
        this.getWalletBalanceUseCase = getWalletBalanceUseCase;
        this.getTransactionHistoryUseCase = getTransactionHistoryUseCase;
    }

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletHttpRequest request) {
        CreateWalletRequest applicationRequest = new CreateWalletRequest(request.customerId(), request.currency());
        Wallet wallet = createWalletUseCase.createWallet(applicationRequest);
        return ResponseEntity.created(URI.create("/api/v1/wallets/" + wallet.id().value()))
                .body(WebMapper.toWalletResponse(wallet));
    }

    @GetMapping("/{walletId}/balance")
    public BalanceResponse getBalance(@PathVariable String walletId) {
        Money balance = getWalletBalanceUseCase.getBalance(walletId);
        return WebMapper.toBalanceResponse(WalletId.from(walletId), balance);
    }

    @GetMapping("/{walletId}/ledger")
    public List<LedgerEntryResponse> getLedger(@PathVariable String walletId) {
        return getTransactionHistoryUseCase.getLedger(walletId)
                .stream()
                .map(WebMapper::toLedgerEntryResponse)
                .toList();
    }
}
