package az.et.fintechtransactionservice.application.port.out;

import az.et.fintechtransactionservice.domain.model.RiskDecision;

public interface FraudCheckPort {

    RiskDecision assess(TransactionRiskAssessment assessment);
}

