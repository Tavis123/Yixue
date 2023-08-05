package com.yixue.base.exception;

/**
 * @author Tavis
 * @date 2023-08-05
 * @desc 和前端约定返回的异常信息模型
 */
public class RestErrorResponse {
    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String message) {
        this.errMessage = message;
    }


}
