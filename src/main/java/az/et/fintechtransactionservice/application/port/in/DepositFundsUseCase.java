package az.et.fintechtransactionservice.application.port.in;

import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;

public interface DepositFundsUseCase {

    TransactionReceipt deposit(DepositFundsRequest request);
}

