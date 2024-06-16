package com.fibank.cashdesk.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.BAD_REQUEST, reason = "Invalid banknotes quantity!")
public class InvalidBanknoteQuantityException extends RuntimeException{

    public InvalidBanknoteQuantityException(String message) {
        super(message);
    }
}
