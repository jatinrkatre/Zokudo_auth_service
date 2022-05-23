
package com.zokudo.sor.dto;

import org.springframework.http.HttpStatus;

public class ApiError {

    private HttpStatus status;
    private String message;
    private Object details;

    public ApiError(final HttpStatus status, final String message) {
        this.status = status;
        this.message = message;
    }

    public ApiError(HttpStatus status, String message, Object details) {
        this(status, message);
        this.details = details;
        this.message=message;
        this.status=status;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public void setStatus(HttpStatus status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getDetails() {
        return details;
    }

    public void setDetails(Object details) {
        this.details = details;
    }
}
