package com.zzy.rpc.message;

import com.zzy.rpc.serialize.Serializer;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RpcDecoder.java
 * @Description TODO
 * @createTime 2022年04月16日 22:34:00
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private Logger logger = LoggerFactory.getLogger(RpcDecoder.class);
    private Class<?> clazz;
    private Serializer serializer;

    public RpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }
    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        logger.debug("start to decode");
        //因为之前编码的时候写入一个Int型，4个字节来表示长度
        if (byteBuf.readableBytes() < 4) {
            return;
        }
        //标记当前读的位置
        byteBuf.markReaderIndex();
        int classNameLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < classNameLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] className = new byte[classNameLength];
        byteBuf.readBytes(className);
        String name = new String(className);
        Class tempClazz = Class.forName(name);

        int dataLength = byteBuf.readInt();
        if (byteBuf.readableBytes() < dataLength) {
            byteBuf.resetReaderIndex();
            return;
        }
        byte[] data = new byte[dataLength];
        //将byteBuf中的数据读入data字节数组
        byteBuf.readBytes(data);
        Object obj = serializer.deserialize(tempClazz, data);
        list.add(obj);
    }
}
