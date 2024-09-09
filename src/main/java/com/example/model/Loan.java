package com.example.model;

public class Loan {
    private String loanId;
    private Double balance;
    private Double interestRate;
    private String customerid;

    public Loan(String loanId, Double balance, Double interestRate, String customerid) {
        this.loanId = loanId;
        this.balance = balance;
        this.interestRate = interestRate;
        this.customerid = customerid;
    }

    public Double getBalance() {
        return balance;
    }

    public String getLoanId() {
        return loanId;
    }

    public Double getInterestRate() {
        return interestRate;
    }

    public String getCustomerid() {
        return customerid;
    }

    // Constructor, Getters y Setters
}