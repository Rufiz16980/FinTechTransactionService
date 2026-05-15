package az.et.fintechtransactionservice.adapter.in.web;

import az.et.fintechtransactionservice.adapter.in.web.dto.DepositHttpRequest;
import az.et.fintechtransactionservice.adapter.in.web.dto.TransactionReceiptResponse;
import az.et.fintechtransactionservice.adapter.in.web.dto.TransferHttpRequest;
import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.in.DepositFundsUseCase;
import az.et.fintechtransactionservice.application.port.in.TransferFundsUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {

    private final DepositFundsUseCase depositFundsUseCase;
    private final TransferFundsUseCase transferFundsUseCase;

    public TransactionController(DepositFundsUseCase depositFundsUseCase, TransferFundsUseCase transferFundsUseCase) {
        this.depositFundsUseCase = depositFundsUseCase;
        this.transferFundsUseCase = transferFundsUseCase;
    }

    @PostMapping("/deposits")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionReceiptResponse deposit(@Valid @RequestBody DepositHttpRequest request) {
        return WebMapper.toReceiptResponse(depositFundsUseCase.deposit(new DepositFundsRequest(
                request.walletId(),
                request.amount(),
                request.currency(),
                request.description())));
    }

    @PostMapping("/transfers")
    @ResponseStatus(HttpStatus.CREATED)
    public TransactionReceiptResponse transfer(@Valid @RequestBody TransferHttpRequest request) {
        return WebMapper.toReceiptResponse(transferFundsUseCase.transfer(new TransferFundsRequest(
                request.fromWalletId(),
                request.toWalletId(),
                request.amount(),
                request.currency(),
                request.description())));
    }
}

