package az.et.fintechtransactionservice.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record LedgerEntryResponse(
        String entryId,
        String transactionId,
        String walletId,
        String type,
        BigDecimal amount,
        String currency,
        BigDecimal resultingBalance,
        Instant createdAt,
        String description) {
}

