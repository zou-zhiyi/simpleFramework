package com.zzy.rpc.message;

import lombok.Data;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcResponse.java
 * @Description TODO
 * @createTime 2022年04月16日 20:36:00
 */
@Data
public class RpcResponse {
    // 调用编号
    private String requestId;
    // 抛出的异常
    private Throwable throwable;
    // 返回结果
    private Object result;

    public void setError(String toString) {
        result = toString;
    }
}
