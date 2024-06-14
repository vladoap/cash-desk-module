package com.fibank.cashdesk.entity;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

public class CashBalanceCurrency {

    private BigDecimal totalBalance;
    private Map<Integer, Integer> denomination;

    public CashBalanceCurrency() {
        this.totalBalance = BigDecimal.ZERO;
        this.denomination = new HashMap<>();
    }

    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public CashBalanceCurrency setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
        return this;
    }

    public Map<Integer, Integer> getDenomination() {
        return denomination;
    }

    public CashBalanceCurrency setDenomination(Map<Integer, Integer> denomination) {
        this.denomination = denomination;
        return this;
    }
}
