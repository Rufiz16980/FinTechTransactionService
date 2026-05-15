package az.et.fintechtransactionservice.application.strategy;

import static org.assertj.core.api.Assertions.assertThat;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.domain.model.Money;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class FeeStrategyTest {

    @Test
    @DisplayName("Should return zero fee when request contains NO_FEE marker")
    void shouldReturnNoFee() {
        NoFeeStrategy strategy = new NoFeeStrategy();
        TransferFundsRequest request = transferRequest("NO_FEE internal transfer");

        assertThat(strategy.supports(request)).isTrue();
        assertThat(strategy.calculate(Money.of("25.00", "AZN")).amount()).isEqualByComparingTo("0.00");
    }

    @Test
    @DisplayName("Should return configured flat fee")
    void shouldReturnFlatFee() {
        FlatFeeStrategy strategy = new FlatFeeStrategy("0.50");

        Money fee = strategy.calculate(Money.of("25.00", "AZN"));

        assertThat(fee.amount()).isEqualByComparingTo("0.50");
        assertThat(fee.currency()).isEqualTo("AZN");
    }

    @Test
    @DisplayName("Should select no-fee strategy before flat fallback")
    void shouldSelectNoFeeBeforeFlatFallback() {
        FeeStrategyFactory factory = new FeeStrategyFactory(List.of(new FlatFeeStrategy("0.50"), new NoFeeStrategy()));

        FeeCalculationStrategy strategy = factory.select(transferRequest("NO_FEE internal transfer"));

        assertThat(strategy).isInstanceOf(NoFeeStrategy.class);
    }

    @Test
    @DisplayName("Should select flat strategy for regular transfer")
    void shouldSelectFlatStrategyForRegularTransfer() {
        FeeStrategyFactory factory = new FeeStrategyFactory(List.of(new NoFeeStrategy(), new FlatFeeStrategy("0.50")));

        FeeCalculationStrategy strategy = factory.select(transferRequest("Dinner split"));

        assertThat(strategy).isInstanceOf(FlatFeeStrategy.class);
    }

    private static TransferFundsRequest transferRequest(String description) {
        return new TransferFundsRequest(
                "wallet-001",
                "wallet-002",
                new BigDecimal("25.00"),
                "AZN",
                description);
    }
}

