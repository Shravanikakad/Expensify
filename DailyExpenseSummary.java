package com.example.expensify;

public class DailyExpenseSummary {
    private String date;
    private double totalAmount;

    // Constructor
    public DailyExpenseSummary(String date, double totalAmount) {
        this.date = date;
        this.totalAmount = totalAmount;
    }

    // Getter for date
    public String getDate() {
        return date;
    }

    // Getter for totalAmount
    public double getTotalAmount() {
        return totalAmount;
    }
}
