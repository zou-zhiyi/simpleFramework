package com.zzy.rpc.serialize;

import com.alibaba.fastjson.JSON;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName JsonSerialization.java
 * @Description TODO
 * @createTime 2022年04月16日 21:19:00
 */
public class JSONSerialization implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
