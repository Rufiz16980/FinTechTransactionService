package az.et.fintechtransactionservice.domain.exception;

public class SameWalletTransferException extends DomainException {

    public SameWalletTransferException(String message) {
        super(message);
    }
}

