package az.et.fintechtransactionservice.application.port.in;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;

public interface TransferFundsUseCase {

    TransactionReceipt transfer(TransferFundsRequest request);
}

