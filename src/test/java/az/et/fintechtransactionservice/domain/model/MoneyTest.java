package az.et.fintechtransactionservice.domain.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import az.et.fintechtransactionservice.domain.exception.InvalidMoneyAmountException;
import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MoneyTest {

    @Test
    @DisplayName("Should normalize amount scale and currency")
    void shouldNormalizeAmountAndCurrency() {
        Money money = Money.of("10.005", "azn");

        assertThat(money.amount()).isEqualByComparingTo("10.01");
        assertThat(money.currency()).isEqualTo("AZN");
    }

    @Test
    @DisplayName("Should add money with same currency")
    void shouldAddSameCurrencyMoney() {
        Money result = Money.of("10.00", "AZN").plus(Money.of("5.25", "AZN"));

        assertThat(result.amount()).isEqualByComparingTo("15.25");
        assertThat(result.currency()).isEqualTo("AZN");
    }

    @Test
    @DisplayName("Should subtract money with same currency")
    void shouldSubtractSameCurrencyMoney() {
        Money result = Money.of("10.00", "AZN").minus(Money.of("4.25", "AZN"));

        assertThat(result.amount()).isEqualByComparingTo("5.75");
    }

    @Test
    @DisplayName("Should reject negative amounts")
    void shouldRejectNegativeAmount() {
        assertThatThrownBy(() -> Money.of("-1.00", "AZN"))
                .isInstanceOf(InvalidMoneyAmountException.class)
                .hasMessageContaining("must not be negative");
    }

    @Test
    @DisplayName("Should reject subtraction that produces a negative amount")
    void shouldRejectNegativeSubtractionResult() {
        Money balance = Money.of("3.00", "AZN");
        Money debit = Money.of("4.00", "AZN");

        assertThatThrownBy(() -> balance.minus(debit))
                .isInstanceOf(InvalidMoneyAmountException.class)
                .hasMessageContaining("cannot produce a negative amount");
    }

    @Test
    @DisplayName("Should reject arithmetic across currencies")
    void shouldRejectCurrencyMismatch() {
        Money azn = Money.of("10.00", "AZN");
        Money usd = Money.of("1.00", "USD");

        assertThatThrownBy(() -> azn.plus(usd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Currency mismatch");
    }

    @Test
    @DisplayName("Should multiply amount with explicit rounding")
    void shouldMultiplyAmount() {
        Money result = Money.of("10.00", "AZN").multiply(new BigDecimal("0.015"));

        assertThat(result.amount()).isEqualByComparingTo("0.15");
    }
}

