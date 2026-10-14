package com.nexthire.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * BadRequestException — Thrown when the request data is invalid or doesn't make sense.
 *
 * Examples:
 *   - Invalid interview category value
 *   - Invalid difficulty level
 *   - Trying to submit a response to an already-completed interview
 *
 * Maps to HTTP 400 Bad Request.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadRequestException extends RuntimeException {

    public BadRequestException(String message) {
        super(message);
    }
}
