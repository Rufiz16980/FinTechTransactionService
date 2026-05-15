package az.et.fintechtransactionservice.application.model;

import java.util.Objects;

public record CreateWalletRequest(String customerId, String currency) {

    public CreateWalletRequest {
        customerId = requireText(customerId, "customerId");
        currency = requireText(currency, "currency").toUpperCase();
    }

    private static String requireText(String value, String fieldName) {
        String normalized = Objects.requireNonNull(value, fieldName + " must not be null").trim();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
        return normalized;
    }
}

