package az.et.fintechtransactionservice.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import az.et.fintechtransactionservice.application.model.DepositFundsRequest;
import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.application.port.in.DepositFundsUseCase;
import az.et.fintechtransactionservice.application.port.in.TransferFundsUseCase;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.RiskDecision;
import az.et.fintechtransactionservice.domain.model.TransactionId;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;
import az.et.fintechtransactionservice.domain.model.TransactionStatus;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.time.Instant;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(TransactionController.class)
@Import(GlobalExceptionHandler.class)
@Execution(ExecutionMode.SAME_THREAD)
class TransactionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DepositFundsUseCase depositFundsUseCase;

    @MockitoBean
    private TransferFundsUseCase transferFundsUseCase;

    @Test
    @DisplayName("Should deposit funds")
    void shouldDepositFunds() throws Exception {
        given(depositFundsUseCase.deposit(any(DepositFundsRequest.class)))
                .willReturn(receipt(TransactionStatus.COMPLETED));

        mockMvc.perform(post("/api/v1/transactions/deposits")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "walletId": "wallet-001",
                                  "amount": 100.00,
                                  "currency": "AZN",
                                  "description": "Initial deposit"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.amount").value(25.00));
    }

    @Test
    @DisplayName("Should transfer funds")
    void shouldTransferFunds() throws Exception {
        given(transferFundsUseCase.transfer(any(TransferFundsRequest.class)))
                .willReturn(receipt(TransactionStatus.COMPLETED));

        mockMvc.perform(post("/api/v1/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromWalletId": "wallet-001",
                                  "toWalletId": "wallet-002",
                                  "amount": 25.00,
                                  "currency": "AZN",
                                  "description": "Dinner split"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.riskStatus").value("APPROVED"));
    }

    @Test
    @DisplayName("Should return validation error for invalid transfer")
    void shouldValidateTransfer() throws Exception {
        mockMvc.perform(post("/api/v1/transactions/transfers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "fromWalletId": "wallet-001",
                                  "toWalletId": "",
                                  "amount": 0,
                                  "currency": "AZN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    private static TransactionReceipt receipt(TransactionStatus status) {
        return TransactionReceipt.builder()
                .transactionId(TransactionId.newId())
                .status(status)
                .sourceWalletId(WalletId.from("wallet-001"))
                .destinationWalletId(WalletId.from("wallet-002"))
                .amount(Money.of("25.00", "AZN"))
                .fee(Money.of("0.50", "AZN"))
                .riskDecision(RiskDecision.approved(12, "low risk"))
                .message("ok")
                .createdAt(Instant.parse("2026-05-14T10:00:00Z"))
                .build();
    }
}
