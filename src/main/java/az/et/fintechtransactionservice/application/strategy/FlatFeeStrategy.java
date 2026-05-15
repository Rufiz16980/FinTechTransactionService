package az.et.fintechtransactionservice.application.strategy;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.domain.model.Money;
import java.math.BigDecimal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FlatFeeStrategy implements FeeCalculationStrategy {

    private final BigDecimal feeAmount;

    public FlatFeeStrategy(@Value("${fintech.fees.flat-transfer-fee:0.50}") String feeAmount) {
        this.feeAmount = new BigDecimal(feeAmount);
    }

    @Override
    public boolean supports(TransferFundsRequest request) {
        return true;
    }

    @Override
    public Money calculate(Money transferAmount) {
        return Money.of(feeAmount, transferAmount.currency());
    }

    @Override
    public int priority() {
        return 100;
    }
}

