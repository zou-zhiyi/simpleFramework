package com.zzy.rpc.client;

import com.alibaba.fastjson.serializer.JSONSerializer;
import com.zzy.rpc.client.handler.ClientHandler;
import com.zzy.rpc.message.RpcDecoder;
import com.zzy.rpc.message.RpcEncoder;
import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.serialize.JSONSerialization;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcNettyClient.java
 * @Description TODO
 * @createTime 2022年04月16日 22:35:00
 */
public class RpcNettyClient {
    private Logger log = LoggerFactory.getLogger(RpcNettyClient.class);

    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private ClientHandler clientHandler;
    private String host;
    private Integer port;
    private Bootstrap bootstrap;
    private static final int MAX_RETRY = 5;
    public RpcNettyClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
    public void connect() throws InterruptedException {
        if (bootstrap != null) {
            connect(bootstrap, host, port, MAX_RETRY);
            return;
        }
        clientHandler = new ClientHandler();
        eventLoopGroup = new NioEventLoopGroup();
        //启动类
        bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                //指定传输使用的Channel
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.TCP_NODELAY, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS,5000)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //添加编码器
                        pipeline.addLast(new RpcEncoder(new JSONSerialization()));
                        //添加解码器
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new JSONSerialization()));
                        //请求处理类
                        pipeline.addLast(clientHandler);
                    }
                });
        connect(bootstrap, host, port, MAX_RETRY);
    }

    /**
     * 失败重连机制，参考Netty入门实战掘金小册
     *
     * @param bootstrap
     * @param host
     * @param port
     * @param retry
     */
    private void connect(Bootstrap bootstrap, String host, int port, int retry) throws InterruptedException {
        if (channel != null) {
            return;
        }
        ChannelFuture channelFuture = bootstrap.connect(host, port).sync().addListener(future -> {
            if (future.isSuccess()) {
                log.info("连接服务端成功");
            } else if (retry == 0) {
                log.error("重试次数已用完，放弃连接");
            } else {
                //第几次重连：
                int order = (MAX_RETRY - retry) + 1;
                //本次重连的间隔
                int delay = 1 << order;
                log.error("{} : 连接失败，第 {} 重连....", new Date(), order);
                bootstrap.config().group().schedule(() -> {
                    try {
                        connect(bootstrap, host, port, retry - 1);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }, delay, TimeUnit.SECONDS);
            }
        });
        channel = channelFuture.channel();
    }

    /**
     * 发送消息,存在超时问题，超时返回null
     *
     * @param request
     * @return
     */
    public RpcResponse send(final RpcRequest request) {
        try {
            channel.writeAndFlush(request).await(3, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            e.printStackTrace();
            return null;
        }
        return clientHandler.getRpcResponse(request.getRequestId());
    }

    @PreDestroy
    public void close() {
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}
