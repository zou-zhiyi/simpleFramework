package com.zzy.core.bean;

import java.io.Serializable;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Result.java
 * @Description TODO
 * @createTime 2022年03月12日 16:55:00
 */
public class Result implements Serializable {
    private static final long serialVersionUID = 1L;

    private int code;
    private Object data;
    private String msg;

    public Result() {

    }

    public Result(int code, Object data, String msg) {
        this.code = code;
        this.data = data;
        this.msg = msg;
    }

    public static Result resultSuccess(Object data) {
        return new Result(200, data,"success");
    }

    public static Result resultFailed(String msg) {
        return new Result(400, null,msg);
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
