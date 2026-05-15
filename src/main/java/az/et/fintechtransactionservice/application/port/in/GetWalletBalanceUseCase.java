package az.et.fintechtransactionservice.application.port.in;

import az.et.fintechtransactionservice.domain.model.Money;

public interface GetWalletBalanceUseCase {

    Money getBalance(String walletId);
}

