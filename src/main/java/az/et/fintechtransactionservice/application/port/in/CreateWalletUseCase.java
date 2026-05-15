package az.et.fintechtransactionservice.application.port.in;

import az.et.fintechtransactionservice.application.model.CreateWalletRequest;
import az.et.fintechtransactionservice.domain.model.Wallet;

public interface CreateWalletUseCase {

    Wallet createWallet(CreateWalletRequest request);
}

