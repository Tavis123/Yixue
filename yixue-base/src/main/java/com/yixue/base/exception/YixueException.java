package com.yixue.base.exception;

/**
 * @author Tavis
 * @date 2023-08-05
 * @desc 自定义异常
 */
public class YixueException extends RuntimeException {

    private String errMessage;

    public YixueException() {
    }

    public YixueException(String errMessage) {
        super(errMessage);
        this.errMessage = errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public static void cast(String message) {
        throw new YixueException(message);
    }

    public static void cast(CommonError commonError) {
        throw new YixueException(commonError.getErrMessage());
    }

}
