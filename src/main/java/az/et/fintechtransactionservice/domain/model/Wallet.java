package az.et.fintechtransactionservice.domain.model;

import az.et.fintechtransactionservice.domain.exception.InsufficientFundsException;
import az.et.fintechtransactionservice.domain.exception.InvalidMoneyAmountException;
import java.util.Objects;

public final class Wallet {

    private final WalletId id;
    private final CustomerId customerId;
    private Money balance;

    public Wallet(WalletId id, CustomerId customerId, Money balance) {
        this.id = Objects.requireNonNull(id, "id must not be null");
        this.customerId = Objects.requireNonNull(customerId, "customerId must not be null");
        this.balance = Objects.requireNonNull(balance, "balance must not be null");
    }

    public static Wallet open(CustomerId customerId, String currency) {
        return new Wallet(WalletId.newId(), customerId, Money.zero(currency));
    }

    public synchronized void credit(Money amount) {
        requirePositive(amount);
        balance = balance.plus(amount);
    }

    public synchronized void debit(Money amount) {
        requirePositive(amount);
        if (!balance.isGreaterThanOrEqual(amount)) {
            throw new InsufficientFundsException("Wallet " + id.value() + " has insufficient funds");
        }
        balance = balance.minus(amount);
    }

    public WalletId id() {
        return id;
    }

    public CustomerId customerId() {
        return customerId;
    }

    public synchronized Money balance() {
        return balance;
    }

    private static void requirePositive(Money amount) {
        Objects.requireNonNull(amount, "amount must not be null");
        if (!amount.isPositive()) {
            throw new InvalidMoneyAmountException("Amount must be greater than zero");
        }
    }
}

