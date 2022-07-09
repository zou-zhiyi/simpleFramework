package com.zzy.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractServiceComponent;
import com.zzy.http.handler.interfaces.HandlerMapping;
import com.zzy.http.request.BasicRequestHandler;
import com.zzy.http.request.MultiRequestHandler;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.FullHttpRequest;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName AbstractHandlerMapping.java
 * @Description (http请求映射抽象类， 用于处理http请求，存储了url到请求处理handler的映射): 已修改，只做处理请求、返回数据，http响应不做，解耦功能
 * @createTime 2022年03月09日 22:19:00
 */
public abstract class AbstractHandlerMapping extends AbstractServiceComponent implements HandlerMapping {


    // 存储请求url到一个处理请求的handler的映射
    protected Map<String, BasicRequestHandler> handlerMap = new ConcurrentHashMap<>();

    /**
     * 注册一个handler，先将其初始化，然后再加入
     * @param methodName         方法名，注意，必须全大写
     * @param uri                url
     * @param requestHandler     用于处理请求的handler
     */
    public void registHandler(String methodName, String uri, BasicRequestHandler requestHandler){
        registHandler(methodName + ":" + uri, requestHandler);
    }

    public void cancelHandler(String methodName, String uri){
        cancelHandler(methodName+":"+uri);
    }

    /**
     * 注册一个handler，先将其初始化，然后再加入
     * @param methodNameAndUri  方法名:URL
     * @param requestHandler    用于处理请求的handler
     */
    @Override
    public void registHandler(String methodNameAndUri, BasicRequestHandler requestHandler){
        if (requestHandler instanceof MultiRequestHandler) {
            MultiRequestHandler multiRequestHandler = (MultiRequestHandler) requestHandler;
            try {
                multiRequestHandler.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            List<String> methodAndUrlList = multiRequestHandler.getMethodAndUrlList();
            for (String methodAndUrl : methodAndUrlList) {
                handlerMap.put(methodAndUrl, multiRequestHandler);
            }
        }
        else {
            try {
                requestHandler.init();
            } catch (Exception e) {
                e.printStackTrace();
            }
            handlerMap.put(methodNameAndUri,requestHandler);
        }
    }

    /**
     * 注销requestHandler
     * @param methodNameAndUri  方法名:URL
     */
    @Override
    public void cancelHandler(String methodNameAndUri){
        handlerMap.remove(methodNameAndUri);
    }


    @Override
    protected void init(Map<String, Object> map) throws Exception {
        super.init(map);
    }

    @Override
    protected void destroy(Map<String, Object> map) throws Exception {
        super.destroy(map);
        handlerMap.clear();
    }

    public Map<String, BasicRequestHandler> getHandlerMap() {
        return handlerMap;
    }

    public void setHandlerMap(Map<String, BasicRequestHandler> handlerMap) {
        this.handlerMap = handlerMap;
    }
}
