package com.aureport.ultra.pub.config.model;

public class SalesRecord {
    private String month;
    private Double amount;
    private Integer count;

    public SalesRecord() {}

    public SalesRecord(String month, Double amount, Integer count) {
        this.month = month;
        this.amount = amount;
        this.count = count;
    }

    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
