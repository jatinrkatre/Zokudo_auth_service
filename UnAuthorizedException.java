package com.zokudo.sor.exceptions;


import org.springframework.core.NestedRuntimeException;

public class UnAuthorizedException extends NestedRuntimeException {

    private String message;
    private  String code;
    private String status;

    public UnAuthorizedException(String message, String code, Throwable cause) {
        super(message, cause);
        this.message = message;
        this.code = code;
    }

    public UnAuthorizedException(String message){
        super(message);
        this.message=message;
    }


    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
