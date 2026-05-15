package az.et.fintechtransactionservice.adapter.in.web.dto;

import java.math.BigDecimal;

public record BalanceResponse(String walletId, BigDecimal balance, String currency) {
}

