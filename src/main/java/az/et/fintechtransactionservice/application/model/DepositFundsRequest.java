package az.et.fintechtransactionservice.application.model;

import java.math.BigDecimal;
import java.util.Objects;

public record DepositFundsRequest(String walletId, BigDecimal amount, String currency, String description) {

    public DepositFundsRequest {
        walletId = requireText(walletId, "walletId");
        amount = Objects.requireNonNull(amount, "amount must not be null");
        currency = requireText(currency, "currency").toUpperCase();
        description = Objects.requireNonNullElse(description, "");
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}

