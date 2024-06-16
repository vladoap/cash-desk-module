package com.fibank.cashdesk.dto;

import com.fibank.cashdesk.enums.CashOperationType;
import com.fibank.cashdesk.enums.Currency;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Map;

public class CashOperationDTO {

    private LocalDateTime localDateTime;
    private CashOperationType type;
    private Currency currency;
    private BigDecimal amount;
    private Map<Integer, Integer> denomination;

    @NotNull(message = "Cash operation type must not be null.")
    public CashOperationType getType() {
        return type;
    }

    public CashOperationDTO setType(CashOperationType type) {
        this.type = type;
        return this;
    }

    @NotNull(message = "Currency must not be null.")
    public Currency getCurrency() {
        return currency;
    }

    public CashOperationDTO setCurrency(Currency currency) {
        this.currency = currency;
        return this;
    }

    @NotNull(message = "Amount must not be null.")
    @Positive(message = "Amount must be positive.")
    public BigDecimal getAmount() {
        return amount;
    }

    public CashOperationDTO setAmount(BigDecimal amount) {
        this.amount = amount;
        return this;
    }


    public Map<Integer, Integer> getDenomination() {
        return denomination;
    }

    public CashOperationDTO setDenomination(Map<Integer, Integer> denomination) {
        this.denomination = denomination;
        return this;
    }

    public LocalDateTime getLocalDateTime() {
        return localDateTime;
    }

    public CashOperationDTO setLocalDateTime(LocalDateTime localDateTime) {
        this.localDateTime = localDateTime;
        return this;
    }
}
