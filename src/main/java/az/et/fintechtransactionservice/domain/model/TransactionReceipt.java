package az.et.fintechtransactionservice.domain.model;

import java.time.Instant;
import java.util.Objects;
import java.util.Optional;

public final class TransactionReceipt {

    private final TransactionId transactionId;
    private final TransactionStatus status;
    private final WalletId sourceWalletId;
    private final WalletId destinationWalletId;
    private final Money amount;
    private final Money fee;
    private final RiskDecision riskDecision;
    private final String message;
    private final Instant createdAt;

    private TransactionReceipt(Builder builder) {
        this.transactionId = Objects.requireNonNull(builder.transactionId, "transactionId must not be null");
        this.status = Objects.requireNonNull(builder.status, "status must not be null");
        this.sourceWalletId = builder.sourceWalletId;
        this.destinationWalletId = builder.destinationWalletId;
        this.amount = Objects.requireNonNull(builder.amount, "amount must not be null");
        this.fee = Objects.requireNonNull(builder.fee, "fee must not be null");
        this.riskDecision = builder.riskDecision;
        this.message = Objects.requireNonNullElse(builder.message, "");
        this.createdAt = Objects.requireNonNull(builder.createdAt, "createdAt must not be null");
    }

    public static Builder builder() {
        return new Builder();
    }

    public TransactionId transactionId() {
        return transactionId;
    }

    public TransactionStatus status() {
        return status;
    }

    public Optional<WalletId> sourceWalletId() {
        return Optional.ofNullable(sourceWalletId);
    }

    public Optional<WalletId> destinationWalletId() {
        return Optional.ofNullable(destinationWalletId);
    }

    public Money amount() {
        return amount;
    }

    public Money fee() {
        return fee;
    }

    public Optional<RiskDecision> riskDecision() {
        return Optional.ofNullable(riskDecision);
    }

    public String message() {
        return message;
    }

    public Instant createdAt() {
        return createdAt;
    }

    public static final class Builder {

        private TransactionId transactionId;
        private TransactionStatus status;
        private WalletId sourceWalletId;
        private WalletId destinationWalletId;
        private Money amount;
        private Money fee;
        private RiskDecision riskDecision;
        private String message;
        private Instant createdAt;

        private Builder() {
        }

        public Builder transactionId(TransactionId transactionId) {
            this.transactionId = transactionId;
            return this;
        }

        public Builder status(TransactionStatus status) {
            this.status = status;
            return this;
        }

        public Builder sourceWalletId(WalletId sourceWalletId) {
            this.sourceWalletId = sourceWalletId;
            return this;
        }

        public Builder destinationWalletId(WalletId destinationWalletId) {
            this.destinationWalletId = destinationWalletId;
            return this;
        }

        public Builder amount(Money amount) {
            this.amount = amount;
            return this;
        }

        public Builder fee(Money fee) {
            this.fee = fee;
            return this;
        }

        public Builder riskDecision(RiskDecision riskDecision) {
            this.riskDecision = riskDecision;
            return this;
        }

        public Builder message(String message) {
            this.message = message;
            return this;
        }

        public Builder createdAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public TransactionReceipt build() {
            return new TransactionReceipt(this);
        }
    }
}

