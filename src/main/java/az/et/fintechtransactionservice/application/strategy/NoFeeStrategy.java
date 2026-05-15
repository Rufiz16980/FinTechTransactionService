package az.et.fintechtransactionservice.application.strategy;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.domain.model.Money;
import org.springframework.stereotype.Component;

@Component
public class NoFeeStrategy implements FeeCalculationStrategy {

    private static final String NO_FEE_MARKER = "NO_FEE";

    @Override
    public boolean supports(TransferFundsRequest request) {
        return request.description().toUpperCase().contains(NO_FEE_MARKER);
    }

    @Override
    public Money calculate(Money transferAmount) {
        return Money.zero(transferAmount.currency());
    }

    @Override
    public int priority() {
        return 0;
    }
}

