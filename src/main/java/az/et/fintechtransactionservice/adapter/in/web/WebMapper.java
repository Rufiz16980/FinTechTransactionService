package az.et.fintechtransactionservice.adapter.in.web;

import az.et.fintechtransactionservice.adapter.in.web.dto.BalanceResponse;
import az.et.fintechtransactionservice.adapter.in.web.dto.LedgerEntryResponse;
import az.et.fintechtransactionservice.adapter.in.web.dto.TransactionReceiptResponse;
import az.et.fintechtransactionservice.adapter.in.web.dto.WalletResponse;
import az.et.fintechtransactionservice.domain.model.LedgerEntry;
import az.et.fintechtransactionservice.domain.model.Money;
import az.et.fintechtransactionservice.domain.model.RiskDecision;
import az.et.fintechtransactionservice.domain.model.TransactionReceipt;
import az.et.fintechtransactionservice.domain.model.Wallet;
import az.et.fintechtransactionservice.domain.model.WalletId;

final class WebMapper {

    private WebMapper() {
    }

    static WalletResponse toWalletResponse(Wallet wallet) {
        return new WalletResponse(
                wallet.id().value(),
                wallet.customerId().value(),
                wallet.balance().amount(),
                wallet.balance().currency());
    }

    static BalanceResponse toBalanceResponse(WalletId walletId, Money balance) {
        return new BalanceResponse(walletId.value(), balance.amount(), balance.currency());
    }

    static LedgerEntryResponse toLedgerEntryResponse(LedgerEntry entry) {
        return new LedgerEntryResponse(
                entry.entryId().value(),
                entry.transactionId().value(),
                entry.walletId().value(),
                entry.type().name(),
                entry.amount().amount(),
                entry.amount().currency(),
                entry.resultingBalance().amount(),
                entry.createdAt(),
                entry.description());
    }

    static TransactionReceiptResponse toReceiptResponse(TransactionReceipt receipt) {
        RiskDecision riskDecision = receipt.riskDecision().orElse(null);
        return new TransactionReceiptResponse(
                receipt.transactionId().value(),
                receipt.status().name(),
                receipt.sourceWalletId().map(WalletId::value).orElse(null),
                receipt.destinationWalletId().map(WalletId::value).orElse(null),
                receipt.amount().amount(),
                receipt.amount().currency(),
                receipt.fee().amount(),
                riskDecision == null ? null : riskDecision.status().name(),
                riskDecision == null ? null : riskDecision.score(),
                receipt.message(),
                receipt.createdAt());
    }
}

