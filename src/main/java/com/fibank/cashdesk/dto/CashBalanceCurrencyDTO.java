package com.fibank.cashdesk.dto;

import java.math.BigDecimal;
import java.util.Map;

public class CashBalanceCurrencyDTO {

    private BigDecimal totalBalance;
    private Map<Integer, Integer> denomination;


    public BigDecimal getTotalBalance() {
        return totalBalance;
    }

    public CashBalanceCurrencyDTO setTotalBalance(BigDecimal totalBalance) {
        this.totalBalance = totalBalance;
        return this;
    }

    public Map<Integer, Integer> getDenomination() {
        return denomination;
    }

    public CashBalanceCurrencyDTO setDenomination(Map<Integer, Integer> denomination) {
        this.denomination = denomination;
        return this;
    }
}
