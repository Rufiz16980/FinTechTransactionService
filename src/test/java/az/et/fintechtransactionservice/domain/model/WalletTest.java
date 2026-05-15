package az.et.fintechtransactionservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import az.et.fintechtransactionservice.domain.exception.InsufficientFundsException;
import az.et.fintechtransactionservice.domain.exception.InvalidMoneyAmountException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class WalletTest {

    @Test
    @DisplayName("Should open wallet with zero balance")
    void shouldOpenWalletWithZeroBalance() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "azn");

        assertThat(wallet.id().value()).isNotBlank();
        assertThat(wallet.customerId().value()).isEqualTo("customer-001");
        assertThat(wallet.balance().amount()).isEqualByComparingTo("0.00");
        assertThat(wallet.balance().currency()).isEqualTo("AZN");
    }

    @Test
    @DisplayName("Should credit positive amount")
    void shouldCreditPositiveAmount() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");

        wallet.credit(Money.of("25.00", "AZN"));

        assertThat(wallet.balance().amount()).isEqualByComparingTo("25.00");
    }

    @Test
    @DisplayName("Should debit available balance")
    void shouldDebitAvailableBalance() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");
        wallet.credit(Money.of("25.00", "AZN"));

        wallet.debit(Money.of("10.00", "AZN"));

        assertThat(wallet.balance().amount()).isEqualByComparingTo("15.00");
    }

    @Test
    @DisplayName("Should reject zero credit")
    void shouldRejectZeroCredit() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");

        assertThatThrownBy(() -> wallet.credit(Money.zero("AZN")))
                .isInstanceOf(InvalidMoneyAmountException.class)
                .hasMessageContaining("greater than zero");
    }

    @Test
    @DisplayName("Should reject debit larger than balance")
    void shouldRejectInsufficientFunds() {
        Wallet wallet = Wallet.open(new CustomerId("customer-001"), "AZN");
        wallet.credit(Money.of("5.00", "AZN"));

        assertThatThrownBy(() -> wallet.debit(Money.of("6.00", "AZN")))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("insufficient funds");
    }
}

