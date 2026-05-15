package az.et.fintechtransactionservice.domain.model;

import java.util.Objects;

public record CustomerId(String value) {

    public CustomerId {
        value = Objects.requireNonNull(value, "customerId must not be null").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("customerId must not be blank");
        }
    }
}

