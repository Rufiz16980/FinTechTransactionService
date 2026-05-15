# API Examples

Run Redis if Docker Desktop is available:

```powershell
docker-compose -f infra/docker-compose.yml up -d
```

Run the app:

```powershell
.\gradlew.bat bootRun
```

## Create Wallets

```powershell
curl -X POST http://localhost:8080/api/v1/wallets `
  -H "Content-Type: application/json" `
  -d "{\"customerId\":\"customer-001\",\"currency\":\"AZN\"}"
```

```powershell
curl -X POST http://localhost:8080/api/v1/wallets `
  -H "Content-Type: application/json" `
  -d "{\"customerId\":\"customer-002\",\"currency\":\"AZN\"}"
```

## Deposit

```powershell
curl -X POST http://localhost:8080/api/v1/transactions/deposits `
  -H "Content-Type: application/json" `
  -d "{\"walletId\":\"WALLET_A\",\"amount\":100.00,\"currency\":\"AZN\",\"description\":\"Initial deposit\"}"
```

## Transfer

```powershell
curl -X POST http://localhost:8080/api/v1/transactions/transfers `
  -H "Content-Type: application/json" `
  -d "{\"fromWalletId\":\"WALLET_A\",\"toWalletId\":\"WALLET_B\",\"amount\":25.00,\"currency\":\"AZN\",\"description\":\"Dinner split\"}"
```

## Balance

```powershell
curl http://localhost:8080/api/v1/wallets/WALLET_A/balance
```

## Ledger

```powershell
curl http://localhost:8080/api/v1/wallets/WALLET_A/ledger
```

## Circuit Breaker Demo

Run this transfer three or more times:

```powershell
curl -X POST http://localhost:8080/api/v1/transactions/transfers `
  -H "Content-Type: application/json" `
  -d "{\"fromWalletId\":\"WALLET_A\",\"toWalletId\":\"WALLET_B\",\"amount\":1.00,\"currency\":\"AZN\",\"description\":\"FAIL_FRAUD\"}"
```

Expected behavior:

```text
The fraud provider fails.
The circuit breaker records failures.
The adapter returns REVIEW_REQUIRED.
No money moves.
Logs show fallback behavior.
```

