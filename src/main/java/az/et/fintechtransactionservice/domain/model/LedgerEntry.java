package az.et.fintechtransactionservice.domain.model;

import java.time.Instant;
import java.util.Objects;

public final class LedgerEntry {

    private final LedgerEntryId entryId;
    private final TransactionId transactionId;
    private final WalletId walletId;
    private final TransactionType type;
    private final Money amount;
    private final Money resultingBalance;
    private final Instant createdAt;
    private final String description;

    private LedgerEntry(Builder builder) {
        this.entryId = Objects.requireNonNull(builder.entryId, "entryId must not be null");
        this.transactionId = Objects.requireNonNull(builder.transactionId, "transactionId must not be null");
        this.walletId = Objects.requireNonNull(builder.walletId, "walletId must not be null");
        this.type = Objects.requireNonNull(builder.type, "type must not be null");
        this.amount = Objects.requireNonNull(builder.amount, "amount must not be null");
        this.resultingBalance = Objects.requireNonNull(builder.resultingBalance, "resultingBalance must not be null");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt must not be null");
        this.description = Objects.requireNonNullElse(builder.description, "");
    }

    public static Builder builder() {
        return new Builder();
    }

    public LedgerEntryId entryId() {
        return entryId;
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public WalletId walletId() {
        return walletId;
    }

    public TransactionType type() {
        return type;
    }

    public Money amount() {
        return amount;
    }

    public Money resultingBalance() {
        return resultingBalance;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public String description() {
        return description;
    }

    public static final class Builder {

        private LedgerEntryId entryId;
        private TransactionId transactionId;
        private WalletId walletId;
        private TransactionType type;
        private Money amount;
        private Money resultingBalance;
        private Instant createdAt;
        private String description;

        private Builder() {
        }

        public Builder entryId(LedgerEntryId entryId) {
            this.entryId = entryId;
            return this;
        }

        public Builder transactionId(TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder walletId(WalletId walletId) {
            this.walletId = walletId;
            return this;
        }

        public Builder type(TransactionType type) {
            this.type = type;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder resultingBalance(Money resultingBalance) {
            this.resultingBalance = resultingBalance;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder description(String description) {
            this.description = description;
            return this;
        }

        public LedgerEntry build() {
            return new LedgerEntry(this);
        }
    }
}

