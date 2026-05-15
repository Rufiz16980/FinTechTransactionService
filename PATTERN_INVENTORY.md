# Pattern Inventory

| Category | Pattern | Classes | Architectural Reason |
|---|---|---|---|
| Creational | Builder | `LedgerEntry`, `TransactionReceipt` | Financial records contain several required fields. Builder construction keeps them readable and immutable after creation. |
| Creational | Factory Method | `TransactionCommandFactory` | The facade asks the factory for the correct per-request command instead of knowing each command constructor. |
| Structural | Adapter | `LegacyFraudRiskAdapter`, `LegacyFraudRiskClient`, `FraudCheckPort` | The application uses a clean typed fraud-check port while the legacy mock provider exposes an ugly string API. |
| Structural | Facade | `TransactionFacade` | Controllers use one application entry point for wallet creation, deposits, transfers, and ledger retrieval. |
| Behavioral | Command | `DepositTransactionCommand`, `TransferTransactionCommand` | Each transaction operation is encapsulated as a short-lived executable object. |
| Behavioral | Strategy | `FeeCalculationStrategy`, `NoFeeStrategy`, `FlatFeeStrategy`, `FeeStrategyFactory` | Fee rules vary without changing transfer orchestration. |

Optional extension point:

| Category | Pattern | Classes | Architectural Reason |
|---|---|---|---|
| Behavioral | Observer | Not implemented in MVP | The design leaves room for audit/cache listeners, but the MVP already satisfies the behavioral requirement with Command and Strategy. |

