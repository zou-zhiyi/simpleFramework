package com.zzy.rpc.message;

import lombok.Data;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcRequest.java
 * @Description TODO
 * @createTime 2022年04月16日 20:32:00
 */
@Data
public class RpcRequest {
    // 心跳检测位
    private Boolean isBeat;
    // 主机id
    private String hostId;
    // 版本号
    private long version;


    // 调用编号
    private String requestId;
    // 接口名
    private String interfaceName;
    // 类名
    private String className;
    // 方法名
    private String methodName;
    // 请求参数的数据类型
    private Class<?>[] parameterTypes;
    // 请求的参数
    private Object[] parameters;
}
