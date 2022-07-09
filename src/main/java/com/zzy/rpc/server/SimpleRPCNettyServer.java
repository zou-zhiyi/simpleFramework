package com.zzy.rpc.server;

import com.zzy.rpc.message.RpcDecoder;
import com.zzy.rpc.message.RpcEncoder;
import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.serialize.JSONSerialization;
import com.zzy.rpc.server.handler.ServerHandler;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName NettyServer.java
 * @Description 简易rpc服务端，不含注册功能，只有接收外界rpc请求的功能
 * @createTime 2022年04月16日 22:40:00
 */
public class SimpleRPCNettyServer {
    private EventLoopGroup boss = null;
    private EventLoopGroup worker = null;

    private ServerHandler serverHandler = new ServerHandler();

    private Logger log = LoggerFactory.getLogger(SimpleRPCNettyServer.class);

    private ServerBootstrap serverBootstrap = new ServerBootstrap();

    private ChannelFuture channelFuture;

    public void start(int port) throws Exception {
        //负责处理客户端连接的线程池
        boss = new NioEventLoopGroup();
        //负责处理读写操作的线程池
        worker = new NioEventLoopGroup();
        serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //添加编码器
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new JSONSerialization()));
                        pipeline.addLast(new RpcDecoder(RpcRequest.class, new JSONSerialization()));
                        //添加解码器
                        pipeline.addLast(new RpcEncoder(new JSONSerialization()));
//                        pipeline.addLast(new RpcEncoder(RpcRequest.class, new JSONSerialization()));
                        //添加请求处理器
                        pipeline.addLast(serverHandler);

                    }
                });
        bind(serverBootstrap, port);

    }

    /**
     * 如果端口绑定失败，端口数+1,重新绑定
     *
     * @param serverBootstrap
     * @param port
     */
    public void bind(final ServerBootstrap serverBootstrap,int port) {
        try {
            channelFuture = serverBootstrap.bind(port).addListener(future -> {
                if (future.isSuccess()) {
                    log.info("端口[ {} ] 绑定成功", port);
                } else {
                    log.error("端口[ {} ] 绑定失败", port);
                    bind(serverBootstrap, port + 1);
                }
            }).sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭Netty");
    }


}
