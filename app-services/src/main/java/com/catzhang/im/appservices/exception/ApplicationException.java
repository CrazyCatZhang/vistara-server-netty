package com.catzhang.im.appservices.exception;


import com.catzhang.im.appservices.enums.ErrorCode;

/**
 * @author crazycatzhang
 */
public class ApplicationException extends RuntimeException {

    private final int code;

    private final String error;


    public ApplicationException(int code, String message) {
        super(message);
        this.code = code;
        this.error = message;
    }

    public ApplicationException(ErrorCode exceptionEnum) {
        super(exceptionEnum.getError());
        this.code = exceptionEnum.getCode();
        this.error = exceptionEnum.getError();
    }

    public int getCode() {
        return code;
    }

    public String getError() {
        return error;
    }


    /**
     * avoid the expensive and useless stack trace for api exceptions
     *
     * @see Throwable#fillInStackTrace()
     */
    @Override
    public Throwable fillInStackTrace() {
        return this;
    }

}
