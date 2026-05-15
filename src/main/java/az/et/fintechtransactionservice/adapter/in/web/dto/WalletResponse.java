package az.et.fintechtransactionservice.adapter.in.web.dto;

import java.math.BigDecimal;

public record WalletResponse(String walletId, String customerId, BigDecimal balance, String currency) {
}

