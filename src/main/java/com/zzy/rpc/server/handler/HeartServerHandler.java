package com.zzy.rpc.server.handler;

import com.alibaba.fastjson.JSONObject;
import com.zzy.core.interfaces.BeanController;
import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.server.loadbalance.ClusterBeanController;
import com.zzy.rpc.test.HelloServiceImpl;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static com.zzy.rpc.utils.RPCConstances.RPC_RESPONSE_OK;
import static com.zzy.rpc.utils.RPCConstances.RPC_RESPONSE_UPDATE;
import static com.zzy.utils.Constances.BEAN_NAME;
import static com.zzy.utils.Constances.HOST_ID;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName HeartServerHandler.java
 * @Description TODO
 * @createTime 2022年04月19日 19:40:00
 */
@ChannelHandler.Sharable
public class HeartServerHandler extends SimpleChannelInboundHandler<RpcRequest> {

    private Logger logger = LoggerFactory.getLogger(HeartServerHandler.class);

    private ClusterBeanController beanController;

    public HeartServerHandler(ClusterBeanController beanController) {
        this.beanController = beanController;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, RpcRequest rpcRequest) throws Exception {
        Boolean isBeat = rpcRequest.getIsBeat();
        if (isBeat != null && isBeat) {
            logger.debug("rpcRequest is {}", JSONObject.toJSONString(rpcRequest));
            boolean checkResult = beanController.checkVersion(rpcRequest.getHostId(), rpcRequest.getVersion(), channelHandlerContext.channel());
            RpcResponse rpcResponse;
            if (!checkResult) {
                rpcResponse = new RpcResponse();
                rpcResponse.setRequestId(rpcRequest.getRequestId());
                rpcResponse.setResult(RPC_RESPONSE_UPDATE);
            }
            else {
                rpcResponse = new RpcResponse();
                rpcResponse.setRequestId(rpcRequest.getRequestId());
                rpcResponse.setResult(RPC_RESPONSE_OK);
            }

            if (rpcRequest.getParameters() != null) {
//                logger.debug("received parameters: {}", JSONObject.toJSONString(rpcRequest.getParameters()));
                Object[] parameters = rpcRequest.getParameters();
                for (Object para : parameters) {
                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put(BEAN_NAME, (String)para);
                    jsonObject.put(HOST_ID, rpcRequest.getHostId());
                    beanController.registBean(jsonObject);
                }
            }
            else {
                logger.debug("received no parameters");
            }
            channelHandlerContext.channel().writeAndFlush(rpcResponse);

        }
    }
}
