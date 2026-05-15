package az.et.fintechtransactionservice.domain.model;

import java.util.Objects;

public record Fee(Money amount) {

    public Fee {
        Objects.requireNonNull(amount, "amount must not be null");
    }

    public static Fee none(String currency) {
        return new Fee(Money.zero(currency));
    }

    public boolean isCharged() {
        return amount.isPositive();
    }
}

