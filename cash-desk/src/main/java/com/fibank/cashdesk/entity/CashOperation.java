package com.fibank.cashdesk.entity;

import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;

import java.math.BigDecimal;
import java.util.Map;

public class CashOperation {

    private CashOperationType type;
    private Currency currency;
    private BigDecimal amount;
    private Map<Integer, Integer> denomination;

    public CashOperationType getType() {
        return type;
    }

    public CashOperation setType(CashOperationType type) {
        this.type = type;
        return this;
    }

    public Currency getCurrency() {
        return currency;
    }

    public CashOperation setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public CashOperation setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }

    public Map<Integer, Integer> getDenomination() {
        return denomination;
    }

    public CashOperation setDenomination(Map<Integer, Integer> denomination) {
        this.denomination = denomination;
        return this;
    }
}
