package az.et.fintechtransactionservice.adapter.in.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import az.et.fintechtransactionservice.application.model.CreateWalletRequest;
import az.et.fintechtransactionservice.application.port.in.CreateWalletUseCase;
import az.et.fintechtransactionservice.application.port.in.GetTransactionHistoryUseCase;
import az.et.fintechtransactionservice.application.port.in.GetWalletBalanceUseCase;
import az.et.fintechtransactionservice.domain.model.CustomerId;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.LedgerEntryId;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.TransactionId;
import az.et.fintechtransactionservice.domain.model.TransactionType;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;
import java.time.Instant;
import java.util.List;
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

@WebMvcTest(WalletController.class)
@Import(GlobalExceptionHandler.class)
@Execution(ExecutionMode.SAME_THREAD)
class WalletControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CreateWalletUseCase createWalletUseCase;

    @MockitoBean
    private GetWalletBalanceUseCase getWalletBalanceUseCase;

    @MockitoBean
    private GetTransactionHistoryUseCase getTransactionHistoryUseCase;

    @Test
    @DisplayName("Should create wallet")
    void shouldCreateWallet() throws Exception {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");
        given(createWalletUseCase.createWallet(any(CreateWalletRequest.class))).willReturn(wallet);

        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "customer-001",
                                  "currency": "AZN"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/wallets/" + wallet.id().value()))
                .andExpect(jsonPath("$.walletId").value(wallet.id().value()))
                .andExpect(jsonPath("$.customerId").value("customer-001"))
                .andExpect(jsonPath("$.currency").value("AZN"));
    }

    @Test
    @DisplayName("Should return validation error for invalid wallet request")
    void shouldValidateCreateWalletRequest() throws Exception {
        mockMvc.perform(post("/api/v1/wallets")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "customerId": "",
                                  "currency": "AZN"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    @DisplayName("Should return wallet balance")
    void shouldReturnBalance() throws Exception {
        given(getWalletBalanceUseCase.getBalance("wallet-001")).willReturn(Money.of("42.50", "AZN"));

        mockMvc.perform(get("/api/v1/wallets/wallet-001/balance"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.walletId").value("wallet-001"))
                .andExpect(jsonPath("$.balance").value(42.50))
                .andExpect(jsonPath("$.currency").value("AZN"));
    }

    @Test
    @DisplayName("Should return wallet ledger")
    void shouldReturnLedger() throws Exception {
        LedgerEntry entry = LedgerEntry.builder()
                .entryId(LedgerEntryId.newId())
                .transactionId(TransactionId.newId())
                .walletId(WalletId.from("wallet-001"))
                .type(TransactionType.DEPOSIT)
                .amount(Money.of("10.00", "AZN"))
                .resultingBalance(Money.of("10.00", "AZN"))
                .createdAt(Instant.parse("2026-05-14T10:00:00Z"))
                .description("Deposit")
                .build();
        given(getTransactionHistoryUseCase.getLedger("wallet-001")).willReturn(List.of(entry));

        mockMvc.perform(get("/api/v1/wallets/wallet-001/ledger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].walletId").value("wallet-001"))
                .andExpect(jsonPath("$[0].type").value("DEPOSIT"))
                .andExpect(jsonPath("$[0].amount").value(10.00));
    }
}
