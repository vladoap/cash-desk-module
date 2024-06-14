package com.fibank.cashdesk.entity;

import com.fibank.cashdesk.enums.Currency;

import java.util.Map;

public class CashBalance {

    private Map<Currency, CashBalanceCurrency> balance;

    public Map<Currency, CashBalanceCurrency> getBalance() {
        return balance;
    }

    public CashBalance setBalance(Map<Currency, CashBalanceCurrency> balance) {
        this.balance = balance;
        return this;
    }
}
