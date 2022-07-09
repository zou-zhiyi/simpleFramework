package com.zzy.rpc.message;

import com.zzy.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcEncoder.java
 * @Description TODO
 * @createTime 2022年04月16日 22:33:00
 */
public class RpcEncoder extends MessageToByteEncoder {
    private Logger logger = LoggerFactory.getLogger(RpcEncoder.class);

    private Serializer serializer;

    public RpcEncoder(Serializer serializer) {
        this.serializer = serializer;
    }


    @Override
    protected void encode(ChannelHandlerContext channelHandlerContext, Object msg, ByteBuf byteBuf) throws Exception {
        Class<?> clazz = Class.forName(msg.getClass().getName());
        logger.debug("encode check, msg type is {}, clazz name is {}",
                msg.getClass().getName(), clazz.getName());
        if (clazz != null && clazz.isInstance(msg)) {
            logger.debug("start to encode");
            byte[] classbytes = clazz.getName().getBytes();
            byte[] bytes = serializer.serialize(msg);
            byteBuf.writeInt(classbytes.length);
            byteBuf.writeBytes(classbytes);
            byteBuf.writeInt(bytes.length);
            byteBuf.writeBytes(bytes);
        }
    }
}
