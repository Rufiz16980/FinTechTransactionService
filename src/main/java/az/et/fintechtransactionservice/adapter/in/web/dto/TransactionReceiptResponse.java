package az.et.fintechtransactionservice.adapter.in.web.dto;

import java.math.BigDecimal;
import java.time.Instant;

public record TransactionReceiptResponse(
        String transactionId,
        String status,
        String sourceWalletId,
        String destinationWalletId,
        BigDecimal amount,
        String currency,
        BigDecimal fee,
        String riskStatus,
        Integer riskScore,
        String message,
        Instant createdAt) {
}

