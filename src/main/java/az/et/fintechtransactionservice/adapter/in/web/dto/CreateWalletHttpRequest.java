package az.et.fintechtransactionservice.adapter.in.web.dto;

import jakarta.validation.constraints.NotBlank;

public record CreateWalletHttpRequest(
        @NotBlank String customerId,
        @NotBlank String currency) {
}

