package com.zokudo.sor.exceptions;

import org.springframework.core.NestedRuntimeException;
import org.springframework.http.HttpStatus;

public class ApiException extends NestedRuntimeException {

    private static final long serialVersionUID = -985255635168987093L;
    private final String code;
    private final String message;
    private final HttpStatus status;

    public ApiException(final String code, final String message, final HttpStatus status, final Throwable cause) {
        super(message, cause);
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public ApiException(final String code, final String message, final HttpStatus status) {
        super(message);
        this.code = code;
        this.message = message;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public HttpStatus getStatus() {
        return status;
    }
}
