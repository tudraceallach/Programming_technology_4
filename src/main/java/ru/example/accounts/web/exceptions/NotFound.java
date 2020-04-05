package ru.example.accounts.web.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * The  Not found exception handler.
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class NotFound extends RuntimeException {
    /**
     * Instantiates a new Not found.
     *
     * @param message the message
     */
    public NotFound(String message) {
        super(message);
    }
}
