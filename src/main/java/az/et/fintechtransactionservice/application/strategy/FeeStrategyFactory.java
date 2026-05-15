package az.et.fintechtransactionservice.application.strategy;

import az.et.fintechtransactionservice.application.model.TransferFundsRequest;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class FeeStrategyFactory {

    private final List<FeeCalculationStrategy> strategies;

    public FeeStrategyFactory(List<FeeCalculationStrategy> strategies) {
        this.strategies = strategies.stream()
                .sorted(Comparator.comparingInt(FeeCalculationStrategy::priority))
                .toList();
    }

    public FeeCalculationStrategy select(TransferFundsRequest request) {
        return strategies.stream()
                .filter(strategy -> strategy.supports(request))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No fee strategy supports request"));
    }
}

