package com.zzy.rpc.client;

import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Date;
import java.util.UUID;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcClientDynamicProxy.java
 * @Description TODO
 * @createTime 2022年04月16日 22:53:00
 */
public class RpcClientDynamicProxy<T> implements InvocationHandler {
    private Logger log = LoggerFactory.getLogger(RpcClientDynamicProxy.class);
    private Class<T> clazz;
    private String implName;
    private String ip;
    private int port;
    private RpcNettyClient rpcNettyClient;

    /**
     *
     * @param clazz 接口的class
     * @param implName 接口实例的className
     * @throws Exception
     */
    public RpcClientDynamicProxy(Class<T> clazz, String implName,String ip, int port) throws Exception {
        this.clazz = clazz;
        this.implName = implName;
        this.ip = ip;
        this.port = port;
    }

    public RpcClientDynamicProxy(Class<T> clazz, String implName,RpcNettyClient rpcNettyClient) throws Exception {
        this.clazz = clazz;
        this.implName = implName;
        this.rpcNettyClient = rpcNettyClient;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
//        String className = method.getDeclaringClass().getName();

//      原本为接口的className，在server端查询接口对应的impl实例，调用其方法，但如果，一个接口有多个实例实现，那么就无法判断为哪一个实例
//      request.setClassName(className);
//      替换为实例的className，可以精确找到对应的impl
        RpcRequest request = new RpcRequest();
        String requestId = UUID.randomUUID().toString();
        String methodName = method.getName();
        Class<?>[] parameterTypes = method.getParameterTypes();
        request.setRequestId(requestId);
        request.setClassName(implName);
        request.setMethodName(methodName);
        request.setParameterTypes(parameterTypes);
        request.setParameters(args);
        log.info("请求内容: {}",request);

        //开启Netty 客户端，直连
        if (rpcNettyClient != null) {
            log.info("开始连接服务端：{}",new Date());
            rpcNettyClient.connect();
            RpcResponse send = rpcNettyClient.send(request);
            log.info("请求调用返回结果：{}", send.getResult());
            return send.getResult();
        }
        else {
            RpcNettyClient nettyClient = new RpcNettyClient(ip, port);
            log.info("开始连接服务端：{}",new Date());
            nettyClient.connect();
            RpcResponse send = nettyClient.send(request);
            nettyClient.close();
            log.info("请求调用返回结果：{}", send.getResult());
            return send.getResult();
        }
    }
}
