package com.fibank.cashdesk.controller;

import com.fibank.cashdesk.dto.CashBalanceDTO;
import com.fibank.cashdesk.dto.CashOperationDTO;
import com.fibank.cashdesk.exception.InvalidApiKeyException;
import com.fibank.cashdesk.service.impl.OperationServiceImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.io.IOException;

@RestController
@RequestMapping("/api/v1")
public class OperationController {

    private final OperationServiceImpl operationService;

    public OperationController(OperationServiceImpl operationServiceImpl) {
        this.operationService = operationServiceImpl;
    }

    @PostMapping("/cash-operation")
    public ResponseEntity<CashOperationDTO> cashOperation(@RequestHeader("FIB-X-AUTH") String apiKey, @Valid @RequestBody CashOperationDTO cashOperation) throws IOException {
        if (!operationService.isApiKeyValid(apiKey)) {
            throw new InvalidApiKeyException("Invalid API Key provided.");
        }

        CashOperationDTO dto = operationService.performCashOperation(cashOperation);
        return ResponseEntity.ok(dto);
    }

    @GetMapping("/cash-balance")
    public ResponseEntity<CashBalanceDTO> getCashBalance(@RequestHeader("FIB-X-AUTH") String apiKey) throws IOException {
        if (!operationService.isApiKeyValid(apiKey)) {
            throw new InvalidApiKeyException("Invalid API Key provided.");
        }

        CashBalanceDTO cashBalanceDTO = operationService.retrieveCashBalance();
        return ResponseEntity.ok(cashBalanceDTO);
    }
}
