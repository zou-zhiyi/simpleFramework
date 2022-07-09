package com.zzy.rpc.server.handler;

import com.zzy.rpc.client.handler.ClientHandler;
import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.server.loadbalance.ClusterBeanController;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ServerLoadBalanceHandler.java
 * @Description TODO
 * @createTime 2022年04月19日 10:55:00
 */
@ChannelHandler.Sharable
public class ServerLoadBalanceHandler extends SimpleChannelInboundHandler<RpcRequest> {
    private Logger log = LoggerFactory.getLogger(ServerHandler.class);

    private ClientHandler clientHandler;

    private ClusterBeanController beanController;

    public ServerLoadBalanceHandler(ClientHandler clientHandler, ClusterBeanController beanController) {
//        CoreManagerImpl.getInstance().registBean(new ClusterBeanController());
        this.clientHandler = clientHandler;
        this.beanController = beanController;
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        log.debug("a event occur: {}", evt.getClass().getName());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent stateEvent = (IdleStateEvent) evt;
            if (IdleState.ALL_IDLE.equals(stateEvent.state())) {
                log.debug("超时一次");
                beanController.cancelHostByChannel(ctx.channel());
            }
        }

    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        log.debug("a channel disconnected");
        beanController.cancelHostByChannel(ctx.channel());
    }


    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //super.exceptionCaught(ctx, cause);
        ctx.close();
        log.error("异常信息：", cause);
        beanController.cancelHostByChannel(ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) {
        if (msg.getIsBeat()!=null && msg.getIsBeat()){
            log.info("request is beat");
            ctx.fireChannelRead(msg);
            return;
        }
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
     * 服务端使用代理处理rpc请求
     *
     * @param request
     * @return
     */
    private Object handler(RpcRequest request) throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, InstantiationException, InterruptedException {
        // 需要一个中央管理类，用于注册所有组件
        log.info("serviceName: {}",request.getClassName());
//        log.info("interfaceName: {}",request.getInterfaceName());
        ;
        Channel channel = (Channel) beanController.getBean(request.getClassName());
        if (channel == null) {
            throw new ClassNotFoundException();
        }
        channel.writeAndFlush(request).await(3, TimeUnit.SECONDS);
        RpcResponse rpcResponse = clientHandler.getRpcResponse(request.getRequestId());
        return rpcResponse.getResult();
    }
}
