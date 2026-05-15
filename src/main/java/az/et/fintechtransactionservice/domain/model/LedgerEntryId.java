package az.et.fintechtransactionservice.domain.model;

import java.util.Objects;
import java.util.UUID;

public record LedgerEntryId(String value) {

    public LedgerEntryId {
        value = Objects.requireNonNull(value, "ledgerEntryId must not be null").trim();
        if (value.isBlank()) {
            throw new IllegalArgumentException("ledgerEntryId must not be blank");
        }
    }

    public static LedgerEntryId newId() {
        return new LedgerEntryId(UUID.randomUUID().toString());
    }
}

