package az.et.fintechtransactionservice.adapter.out.fraud.legacy;

import az.et.fintechtransactionservice.adapter.out.fraud.FraudProviderUnavailableException;
import java.math.BigDecimal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LegacyFraudRiskClient {

    private static final Logger log = LoggerFactory.getLogger(LegacyFraudRiskClient.class);

    private final String outageKeyword;

    public LegacyFraudRiskClient(@Value("${fintech.fraud.outage-keyword:FAIL_FRAUD}") String outageKeyword) {
        this.outageKeyword = outageKeyword;
    }

    public String chk(String payload) {
        log.info("Legacy fraud payload received: {}", payload);
        if (payload.contains(outageKeyword)) {
            throw new FraudProviderUnavailableException("Legacy fraud provider is unavailable");
        }

        BigDecimal amount = amountFrom(payload);
        if (amount.compareTo(new BigDecimal("5000.00")) >= 0) {
            return "BLOCK|score=95";
        }
        if (amount.compareTo(new BigDecimal("1000.00")) >= 0) {
            return "REVIEW|score=70";
        }
        return "OK|score=12";
    }

    private BigDecimal amountFrom(String payload) {
        String marker = "AMT=";
        int start = payload.indexOf(marker);
        if (start < 0) {
            return BigDecimal.ZERO;
        }
        int valueStart = start + marker.length();
        int valueEnd = payload.indexOf('|', valueStart);
        String value = valueEnd < 0 ? payload.substring(valueStart) : payload.substring(valueStart, valueEnd);
        return new BigDecimal(value);
    }
}

