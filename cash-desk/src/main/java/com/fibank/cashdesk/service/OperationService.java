package com.fibank.cashdesk.service;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;

import java.io.IOException;

public interface OperationService {

     boolean isApiKeyValid(String apiKey);

    CashBalanceDTO retrieveCashBalance() throws IOException;

    public CashOperationDTO performCashOperation(CashOperationDTO cashOperationDTO) throws IOException;
}
