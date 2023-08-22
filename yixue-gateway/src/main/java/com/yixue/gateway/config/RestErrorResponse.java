package com.yixue.gateway.config;

import java.io.Serializable;

/**
 * @author Tavis
 * @date 2023-08-20
 * @description 错误响应参数类
 */

public class RestErrorResponse implements Serializable {

    private String errMessage;

    public RestErrorResponse(String errMessage) {
        this.errMessage = errMessage;
    }

    public String getErrMessage() {
        return errMessage;
    }

    public void setErrMessage(String errMessage) {
        this.errMessage = errMessage;
    }
}
