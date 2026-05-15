# FinTechTransactionService

FinTechTransactionService is a backend for a FinTech wallet. It supports wallet creation, deposits, transfers, balance lookup, and ledger history while demonstrating design patterns, SOLID principles, Java 21 virtual threads, Redis cache-aside, and a circuit breaker around a mock legacy fraud provider.

## Tech Stack

- Java 21
- Spring Boot 3.4.1
- Gradle
- Spring Web
- Spring Validation
- Spring Data Redis
- Spring Actuator
- Resilience4j
- JUnit 5
- Mockito
- AssertJ
- ArchUnit
- JaCoCo
- Checkstyle

## Architecture

The service uses a compact hexagonal architecture:

- `domain`: pure financial model and domain exceptions
- `application`: inbound ports, outbound ports, commands, factory, facade, strategies
- `adapter`: REST controllers, Redis cache adapter, in-memory persistence, legacy fraud adapter
- `infrastructure`: Spring technical configuration

See [docs/architecture.md](docs/architecture.md).

## Business Features

- Create a wallet for a customer.
- Deposit funds into a wallet.
- Transfer funds between wallets.
- Charge a flat transfer fee.
- Store immutable ledger entries.
- Read wallet balance through Redis cache-aside.
- Run mock fraud checks before transfers.
- Use circuit breaker fallback when fraud provider fails.

## Design Patterns

| Category | Pattern | Classes |
|---|---|---|
| Creational | Builder | `LedgerEntry`, `TransactionReceipt` |
| Creational | Factory Method | `TransactionCommandFactory` |
| Structural | Adapter | `LegacyFraudRiskAdapter` |
| Structural | Facade | `TransactionFacade` |
| Behavioral | Command | `DepositTransactionCommand`, `TransferTransactionCommand` |
| Behavioral | Strategy | `FeeCalculationStrategy`, `NoFeeStrategy`, `FlatFeeStrategy` |

See [PATTERN_INVENTORY.md](PATTERN_INVENTORY.md).

## Redis Cache-Aside

`GetWalletBalanceService` owns cache-aside behavior. It checks Redis first, loads from the wallet repository on cache miss, writes the loaded balance back to Redis, and returns the result. Deposit and transfer commands evict affected wallet balances.

Redis failures are handled gracefully:

- get failure becomes a cache miss
- put failure logs and continues
- evict failure logs and continues

## Circuit Breaker

`LegacyFraudRiskAdapter` wraps `LegacyFraudRiskClient` with Resilience4j. The mock legacy provider can be forced to fail by using this transfer description:

```text
FAIL_FRAUD
```

The fallback decision is `REVIEW_REQUIRED`, and no money moves.

## Virtual Threads

Virtual threads are enabled in `application.yml`:

```yaml
spring:
  threads:
    virtual:
      enabled: true
```

## Running Tests

```powershell
.\gradlew.bat clean test
```

Coverage:

```powershell
.\gradlew.bat jacocoTestReport jacocoTestCoverageVerification
```

Full verification:

```powershell
.\gradlew.bat clean check
```

## Running Locally

Start Redis:

```powershell
docker-compose -f infra/docker-compose.yml up -d
```

Start the app:

```powershell
.\gradlew.bat bootRun
```

If Redis is not running, the application still starts. Balance reads fall back to repository access and log cache warnings.

## API Examples

See [docs/api-examples.md](docs/api-examples.md).

## Refactoring Case Study

See [REFACTORING_CASE_STUDY.md](REFACTORING_CASE_STUDY.md).

## Notes

The project is intentionally lightweight in infrastructure but production-style in boundaries. The persistence adapter is in-memory for a self-contained capstone demo. Redis is still implemented as a real cache adapter, and the fraud provider is a mock external dependency wrapped with Adapter and Circuit Breaker patterns.

