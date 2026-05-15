package az.et.fintechtransactionservice.adapter.in.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;

public record TransferHttpRequest(
        @NotBlank String fromWalletId,
        @NotBlank String toWalletId,
        @NotNull @DecimalMin("0.01") BigDecimal amount,
        @NotBlank String currency,
        String description) {
}

