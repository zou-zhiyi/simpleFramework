package com.zzy.rpc.server.handler;

import com.zzy.core.CoreManagerImpl;
import com.zzy.core.interfaces.CoreManager;
import com.zzy.rpc.client.RpcClientDynamicProxy;
import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.test.HelloService;
import com.zzy.rpc.test.HelloServiceImpl;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import net.sf.cglib.reflect.FastClass;
import net.sf.cglib.reflect.FastMethod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ServerHandler.java
 * @Description TODO
 * @createTime 2022年04月16日 22:49:00
 */
@ChannelHandler.Sharable
public class ServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Logger log = LoggerFactory.getLogger(ServerHandler.class);

    public ServerHandler() {
        // 测试用
        CoreManagerImpl.getInstance().registBean(new HelloServiceImpl());
    }
    // 测试用
//    private Map<String, Object> interfaceMap = new ConcurrentHashMap<String, Object>(){
//        {
//            put(HelloServiceImpl.class.getName(), new HelloServiceImpl());
//        }
//    };

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        RpcResponse rpcResponse = new RpcResponse();
        rpcResponse.setRequestId(msg.getRequestId());
        try {
            Object handler = handler(msg);
            log.info("获取返回结果: {} ", handler);
            rpcResponse.setResult(handler);
        } catch (Throwable throwable) {
            rpcResponse.setError(throwable.toString());
            throwable.printStackTrace();
        }
        ctx.writeAndFlush(rpcResponse);
    }

    /**
     * 服务端使用代理处理请求
     *
     * @param request
     * @return
     */
    private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException {

        // 需要一个中央管理类，用于注册所有组件
        log.info("serviceName: {}",request.getClassName());
        Class<?> clazz = Class.forName(request.getClassName());

        //测试用
//        Object serviceBean = interfaceMap.get(request.getClassName());

        Object serviceBean = CoreManagerImpl.getInstance().getBean(request.getClassName());
        log.info("serviceBean: {}",serviceBean);
        Class<?> serviceClass = serviceBean.getClass();
        log.info("serverClass:{}",serviceClass);
        String methodName = request.getMethodName();
        log.info("serverMethod: {}",methodName);

        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //使用CGLIB Reflect
        FastClass fastClass = FastClass.create(serviceClass);
        FastMethod fastMethod = fastClass.getMethod(methodName, parameterTypes);
        log.info("开始调用CGLIB动态代理执行服务端方法...");
        return fastMethod.invoke(serviceBean, parameters);
    }

}
