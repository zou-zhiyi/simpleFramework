package com.zzy.rpc.serialize;

import java.io.IOException;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Serialization.java
 * @Description TODO
 * @createTime 2022年04月16日 21:17:00
 */
public interface Serializer {
    /**
     * java对象转换为二进制
     *
     * @param object
     * @return
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 二进制转换成java对象
     *
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     */
    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
