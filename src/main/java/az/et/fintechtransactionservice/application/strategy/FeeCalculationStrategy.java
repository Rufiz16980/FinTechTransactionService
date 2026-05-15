package az.et.fintechtransactionservice.application.strategy;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import az.et.fintechtransactionservice.domain.model.Money;

public interface FeeCalculationStrategy {

    boolean supports(TransferFundsRequest request);

    Money calculate(Money transferAmount);

    int priority();
}

