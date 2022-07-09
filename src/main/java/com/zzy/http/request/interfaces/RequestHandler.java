package com.zzy.http.request.interfaces;

import com.alibaba.fastjson.JSONObject;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;

import java.util.Map;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName RequestHandler.java
 * @Description TODO
 * @createTime 2022年02月23日 09:59:00
 */
public interface RequestHandler {
    JSONObject doRequest(Map<String, Object> parameters);
}
