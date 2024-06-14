package com.fibank.cashdesk.dto;


import com.fibank.cashdesk.enums.Currency;

import java.time.LocalDateTime;
import java.util.Map;

public class CashBalanceDTO {

    private LocalDateTime localDateTime;
    private Map<Currency, CashBalanceCurrencyDTO> balance;

    public Map<Currency, CashBalanceCurrencyDTO> getBalance() {
        return balance;
    }

    public CashBalanceDTO setBalance(Map<Currency, CashBalanceCurrencyDTO> balance) {
        this.balance = balance;
        return this;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public CashBalanceDTO setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        return this;
    }
}
