package az.et.fintechtransactionservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public record WalletId(String value) {

    public WalletId {
        value = normalize(value, "walletId");
    }

    public static WalletId newId() {
        return new WalletId(UUID.randomUUID().toString());
    }

    public static WalletId from(String value) {
        return new WalletId(value);
    }

    private static String normalize(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}

