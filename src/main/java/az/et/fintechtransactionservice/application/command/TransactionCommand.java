package az.et.fintechtransactionservice.application.command;

import az.et.fintechtransactionservice.domain.model.TransactionReceipt;

public interface TransactionCommand {

    TransactionReceipt execute();
}

