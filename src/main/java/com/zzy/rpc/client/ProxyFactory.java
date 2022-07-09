package com.zzy.rpc.client;

import java.lang.reflect.Proxy;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ProxyFactory.java
 * @Description TODO
 * @createTime 2022年04月16日 22:57:00
 */
public class ProxyFactory {
    public static <T> T create(Class<T> interfaceClass, String implName, String ip, int port) throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[] {interfaceClass}
        , new RpcClientDynamicProxy<T>(interfaceClass, implName, ip, port));
    }

    public static <T> T create(Class<T> interfaceClass, String implName, RpcNettyClient rpcNettyClient) throws Exception {
        return (T) Proxy.newProxyInstance(interfaceClass.getClassLoader(),new Class<?>[] {interfaceClass}
                , new RpcClientDynamicProxy<T>(interfaceClass, implName, rpcNettyClient));
    }
}
