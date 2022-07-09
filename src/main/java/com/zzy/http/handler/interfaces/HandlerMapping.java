package com.zzy.http.handler.interfaces;

import com.alibaba.fastjson.JSONObject;
import com.zzy.http.request.BasicRequestHandler;

import java.util.Map;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName HandlerMapping.java
 * @Description TODO
 * @createTime 2022年04月18日 09:45:00
 */
public interface HandlerMapping {
    void registHandler(String key, BasicRequestHandler requestHandler);
    void cancelHandler(String key);
    JSONObject doRequest(Map<String, Object> extendParameters, Map<String, Object> requestParameters);
}
