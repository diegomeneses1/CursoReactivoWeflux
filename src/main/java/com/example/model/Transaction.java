package com.example.model;

public class Transaction {
    private String transactionId;
    private String accountId;
    private Double amount;


    public Transaction(String transactionId, String accountId, Double amount) {
        this.transactionId = transactionId;
        this.accountId = accountId;
        this.amount = amount;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getAccountId() {
        return accountId;
    }

    public Double getAmount() {
        return amount;
    }
}
