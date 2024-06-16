package com.fibank.cashdesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid banknote denomination!")
public class InvalidBanknoteDenominationException extends RuntimeException {

    public InvalidBanknoteDenominationException(String message) {
        super(message);
    }
}

