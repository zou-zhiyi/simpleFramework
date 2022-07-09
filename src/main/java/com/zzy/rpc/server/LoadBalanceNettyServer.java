package com.zzy.rpc.server;

import com.sun.corba.se.impl.presentation.rmi.ExceptionHandler;
import com.zzy.core.CoreManagerImpl;
import com.zzy.rpc.client.handler.ClientHandler;
import com.zzy.rpc.message.RpcDecoder;
import com.zzy.rpc.message.RpcEncoder;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.serialize.JSONSerialization;
import com.zzy.rpc.server.handler.HeartServerHandler;
import com.zzy.rpc.server.handler.ServerLoadBalanceHandler;
import com.zzy.rpc.server.loadbalance.ClusterBeanController;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.timeout.IdleStateHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.concurrent.TimeUnit;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName LoadBalanceNettyHandler.java
 * @Description TODO
 * @createTime 2022年04月19日 17:41:00
 */
public class LoadBalanceNettyServer {
    private EventLoopGroup boss = null;
    private EventLoopGroup worker = null;

    private ServerLoadBalanceHandler loadBalanceHandler;
    private ClientHandler clientHandler = new ClientHandler();
    private ClusterBeanController beanController = new ClusterBeanController();
    private HeartServerHandler heartServerHandler;

    private Logger log = LoggerFactory.getLogger(SimpleRPCNettyServer.class);

    public void start(int port) throws Exception {
        //负责处理客户端连接的线程池
        boss = new NioEventLoopGroup();
        //负责处理读写操作的线程池
        worker = new NioEventLoopGroup();
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        loadBalanceHandler = new ServerLoadBalanceHandler(clientHandler, beanController);
        serverBootstrap.group(boss, worker)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        //添加解码器
                        pipeline.addLast(new RpcEncoder(new JSONSerialization()));
                        //添加编码器
                        pipeline.addLast(new RpcDecoder(RpcResponse.class, new JSONSerialization()));
                        pipeline.addLast(new IdleStateHandler(0, 0, 10, TimeUnit.SECONDS));
                        //添加请求处理器
                        pipeline.addLast(loadBalanceHandler);
                        pipeline.addLast(clientHandler);
                        pipeline.addLast(new HeartServerHandler(beanController));

                    }
                });
        bind(serverBootstrap, port);
        // 将bean管理器也加入到实例中
        CoreManagerImpl.getInstance().registBean(beanController);
    }

    /**
     * 如果端口绑定失败，端口数+1,重新绑定
     *
     * @param serverBootstrap
     * @param port
     */
    public void bind(final ServerBootstrap serverBootstrap,int port) {
        serverBootstrap.bind(port).addListener(future -> {
            if (future.isSuccess()) {
                log.info("端口[ {} ] 绑定成功",port);
            } else {
                log.error("端口[ {} ] 绑定失败", port);
                bind(serverBootstrap, port + 1);
            }
        });
    }

    @PreDestroy
    public void destory() throws InterruptedException {
        boss.shutdownGracefully().sync();
        worker.shutdownGracefully().sync();
        log.info("关闭Netty");
    }
}
