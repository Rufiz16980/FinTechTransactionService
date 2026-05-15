package az.et.fintechtransactionservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public record TransactionId(String value) {

    public TransactionId {
        value = Objects.requireNonNull(value, "transactionId must not be null").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("transactionId must not be blank");
        }
    }

    public static TransactionId newId() {
        return new TransactionId(UUID.randomUUID().toString());
    }
}

