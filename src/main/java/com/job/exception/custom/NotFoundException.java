package com.job.exception.custom;

import lombok.Getter;

@Getter
public class NotFoundException extends RuntimeException {

    private final String errorMessage;
    private final String errorCode;

    public NotFoundException(String errorMessage, String errorCode) {
        super(errorMessage);

        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
    }

}
