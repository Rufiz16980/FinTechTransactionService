package az.et.fintechtransactionservice.domain.model;

import az.et.fintechtransactionservice.domain.exception.InvalidMoneyAmountException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

public record Money(BigDecimal amount, String currency) {

    private static final int SCALE = 2;

    public Money {
        Objects.requireNonNull(amount, "amount must not be null");
        currency = normalizeCurrency(currency);
        amount = amount.setScale(SCALE, RoundingMode.HALF_UP);
        if (amount.signum() < 0) {
            throw new InvalidMoneyAmountException("Money amount must not be negative");
        }
    }

    public static Money of(String amount, String currency) {
        return new Money(new BigDecimal(amount), currency);
    }

    public static Money of(BigDecimal amount, String currency) {
        return new Money(amount, currency);
    }

    public static Money zero(String currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public Money plus(Money other) {
        requireSameCurrency(other);
        return new Money(amount.add(other.amount), currency);
    }

    public Money minus(Money other) {
        requireSameCurrency(other);
        BigDecimal result = amount.subtract(other.amount);
        if (result.signum() < 0) {
            throw new InvalidMoneyAmountException("Money subtraction cannot produce a negative amount");
        }
        return new Money(result, currency);
    }

    public Money multiply(BigDecimal multiplier) {
        Objects.requireNonNull(multiplier, "multiplier must not be null");
        return new Money(amount.multiply(multiplier), currency);
    }

    public boolean isPositive() {
        return amount.signum() > 0;
    }

    public boolean isZero() {
        return amount.signum() == 0;
    }

    public boolean isGreaterThanOrEqual(Money other) {
        requireSameCurrency(other);
        return amount.compareTo(other.amount) >= 0;
    }

    private void requireSameCurrency(Money other) {
        Objects.requireNonNull(other, "other money must not be null");
        if (!currency.equals(other.currency)) {
            throw new IllegalArgumentException("Currency mismatch: " + currency + " vs " + other.currency);
        }
    }

    private static String normalizeCurrency(String currency) {
        String normalized = Objects.requireNonNull(currency, "currency must not be null").trim().toUpperCase();
        if (normalized.isBlank()) {
            throw new IllegalArgumentException("currency must not be blank");
        }
        return normalized;
    }
}

