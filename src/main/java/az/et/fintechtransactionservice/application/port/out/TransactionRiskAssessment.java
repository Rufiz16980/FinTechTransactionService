package az.et.fintechtransactionservice.application.port.out;

import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.util.Objects;

public record TransactionRiskAssessment(
        WalletId fromWalletId,
        WalletId toWalletId,
        Money amount,
        String description) {

    public TransactionRiskAssessment {
        Objects.requireNonNull(fromWalletId, "fromWalletId must not be null");
        Objects.requireNonNull(toWalletId, "toWalletId must not be null");
        Objects.requireNonNull(amount, "amount must not be null");
        description = Objects.requireNonNullElse(description, "");
    }
}

