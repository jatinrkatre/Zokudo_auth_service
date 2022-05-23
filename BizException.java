package com.zokudo.sor.exceptions;


import org.springframework.core.NestedRuntimeException;

public class BizException extends NestedRuntimeException {

    private static final long serialVersionUID = 1L;
    private String errorMessage;
    private String additionalMessage;
    private String status;

    public BizException(String errorMessage, String additionalMessage, Throwable cause) {
        super(additionalMessage, cause);
        this.errorMessage = errorMessage;
        this.additionalMessage = additionalMessage;
    }

    public BizException(String errorMessage, Throwable cause) {
        super(errorMessage, cause);
    }

    public BizException(String errorMessage, String additionalMessage) {
        super(additionalMessage);
        this.errorMessage = errorMessage;
        this.additionalMessage = additionalMessage;
    }

    public BizException(String additionalMessage) {
        super(additionalMessage);
        this.additionalMessage = additionalMessage;
    }


    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public String getAdditionalMessage() {
        return additionalMessage;
    }

    public void setAdditionalMessage(String additionalMessage) {
        this.additionalMessage = additionalMessage;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
