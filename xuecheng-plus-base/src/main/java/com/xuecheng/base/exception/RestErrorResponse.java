package com.xuecheng.base.exception;

import java.io.Serializable;

/**
 * @author jxchen
 * @version 1.0
 * @description 错误参数响应包装
 * @date 2023/9/9 20:35
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
