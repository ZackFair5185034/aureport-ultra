package com.aureport.ultra.pub.config.model;

import com.aureport.ultra.core.annotation.FieldDesc;

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

    @FieldDesc("月份")
    public String getMonth() { return month; }
    public void setMonth(String month) { this.month = month; }
    @FieldDesc("金额")
    public Double getAmount() { return amount; }
    public void setAmount(Double amount) { this.amount = amount; }
    @FieldDesc("数量")
    public Integer getCount() { return count; }
    public void setCount(Integer count) { this.count = count; }
}
