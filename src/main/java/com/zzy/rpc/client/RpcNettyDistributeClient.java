package com.zzy.rpc.client;

import com.zzy.core.CoreManagerImpl;
import com.zzy.rpc.client.handler.ClientHandler;
import com.zzy.rpc.message.RpcDecoder;
import com.zzy.rpc.message.RpcEncoder;
import com.zzy.rpc.message.RpcRequest;
import com.zzy.rpc.message.RpcResponse;
import com.zzy.rpc.serialize.JSONSerialization;
import com.zzy.rpc.server.handler.ServerHandler;
import com.zzy.rpc.utils.RPCConstances;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PreDestroy;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static com.zzy.rpc.utils.RPCConstances.*;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcNettyDistributeClient.java
 * @Description 注册bean到服务端，同时接收服务端的rpc请求
 * @createTime 2022年04月20日 09:48:00
 */
public class RpcNettyDistributeClient {
    private Logger log = LoggerFactory.getLogger(RpcNettyDistributeClient.class);

    private EventLoopGroup eventLoopGroup;
    private Channel channel;
    private ClientHandler clientHandler;
    private String host;
    private Integer port;
    private static final int MAX_RETRY = 5;
    public RpcNettyDistributeClient(String host, Integer port) {
        this.host = host;
        this.port = port;
    }
    public void connect() throws InterruptedException {
        clientHandler = new ClientHandler();
        eventLoopGroup = new NioEventLoopGroup();
        //启动类
        Bootstrap bootstrap = new Bootstrap();
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

                        pipeline.addLast(new ServerHandler());
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
     * 发送消息
     *
     * @param request
     * @return
     */
    public RpcResponse send(final RpcRequest request) {
        try {
            channel.writeAndFlush(request).await();
            log.debug("not write yet");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return clientHandler.getRpcResponse(request.getRequestId());
    }

    /**
     * 定时向中央服务器发送心跳包， 同时注册自己拥有的实例
     */
    public void heartBeat() {
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                RpcRequest rpcRequest = new RpcRequest();
                rpcRequest.setRequestId(UUID.randomUUID().toString());
                rpcRequest.setHostId(CoreManagerImpl.getInstance().getHostId());
                rpcRequest.setIsBeat(true);
                rpcRequest.setVersion(CoreManagerImpl.getInstance().getVersion());
//                rpcRequest.setParameters(CoreManagerImpl.getInstance().getBeanList().toArray());
                RpcResponse send = send(rpcRequest);
                String result = (String) send.getResult();
                switch (result) {
                    case RPC_RESPONSE_OK:
                        break;
                    case RPC_RESPONSE_UPDATE:
                        RpcRequest updateRequest = new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setIsBeat(true);
                        rpcRequest.setHostId(CoreManagerImpl.getInstance().getHostId());
                        rpcRequest.setVersion(CoreManagerImpl.getInstance().getVersion());
                        rpcRequest.setParameters(CoreManagerImpl.getInstance().getBeanList().toArray());
                        RpcResponse update = send(rpcRequest);
                        break;
                    default:
                }
//                log.debug("heart beat result is {}", send.getResult());
            }
        },0, 5000);
    }

    @PreDestroy
    public void close() {
        eventLoopGroup.shutdownGracefully();
        channel.closeFuture().syncUninterruptibly();
    }
}
