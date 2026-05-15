# Refactoring Case Study: Legacy Fraud Risk Adapter

## Problem

The transfer workflow needs a fraud check before money moves between wallets. A real external fraud vendor would require credentials and network setup, so this capstone uses a deterministic legacy mock called `LegacyFraudRiskClient`.

The legacy client intentionally has a poor API:

```text
String chk(String payload)
```

The payload is pipe-delimited:

```text
FROM=wallet-1|TO=wallet-2|AMT=100.00|CUR=AZN|DESC=Dinner
```

The response is also a magic string:

```text
OK|score=12
REVIEW|score=70
BLOCK|score=95
```

## Legacy Symptoms

- String payloads instead of typed request objects.
- Magic response values instead of enums or domain objects.
- Hard-coded thresholds inside the legacy client.
- Runtime failure simulation through the `FAIL_FRAUD` keyword.
- No clean distinction between provider failure and fraud rejection.

## Before Refactoring

A tightly coupled transfer service would have to:

- Build the pipe-delimited payload.
- Call `chk`.
- Parse magic strings.
- Decide what `OK`, `REVIEW`, and `BLOCK` mean.
- Handle provider failures.
- Know too much about fraud-provider protocol details.

That would violate SRP and DIP because transfer orchestration would be mixed with external protocol translation.

## After Refactoring

The application core depends only on:

```java
FraudCheckPort
```

The adapter handles the ugly protocol:

```text
LegacyFraudRiskAdapter -> LegacyFraudRiskClient
```

The application receives a typed result:

```text
RiskDecision
```

Resilience is also applied at the adapter boundary through Resilience4j. If the legacy provider fails or the circuit is open, the adapter returns:

```text
REVIEW_REQUIRED
```

## Result

- Transfer commands stay focused on transaction orchestration.
- Legacy string parsing is isolated.
- The fraud provider can be replaced without changing application logic.
- Circuit breaker behavior is testable.
- The refactoring demonstrates Adapter, DIP, SRP, and resilience in one place.

