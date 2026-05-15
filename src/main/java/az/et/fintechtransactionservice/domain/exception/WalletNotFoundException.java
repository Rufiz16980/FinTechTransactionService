package az.et.fintechtransactionservice.domain.exception;

public class WalletNotFoundException extends DomainException {

    public WalletNotFoundException(String walletId) {
        super("Wallet not found: " + walletId);
    }
}

