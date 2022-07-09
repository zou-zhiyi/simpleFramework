package com.zzy.http.request;

import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractComponent;
import com.zzy.http.request.interfaces.RequestHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

import static com.zzy.http.utils.HttpConstances.*;


/**
 * @author admin
 * @version 1.0.0
 * @ClassName SimpleRequestHandler.java
 * @Description requestHandler的基础类，实现了RequestHandler接口
 * @createTime 2022年02月25日 09:13:00
 */
public abstract class BasicRequestHandler extends AbstractComponent implements RequestHandler {

    private Logger logger = LoggerFactory.getLogger(BasicRequestHandler.class);

    public BasicRequestHandler(){

    }

    @Override
    public JSONObject doRequest(Map<String, Object> parameters){
//        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
//        String uri = queryStringDecoder.rawPath();
//        logger.debug("the request uri is {}",uri);
//        Map<String, Object> map = new HashMap<>();
//        map.put(HTTP_HEADER, fullHttpRequest.headers());
//        map.put(HTTP_CONTENT, fullHttpRequest.content());
//        map.put(HTTP_PARAMETERS, parameters);
//        map.put(HTTP_URI, uri);
//        map.put(HTTP_VERSION, fullHttpRequest.protocolVersion());
//        map.put(HTTP_RESPONSE, new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.BAD_REQUEST));
        // 设计不当，考虑修改
        beforeRequest(parameters);
        request(parameters);
        afterRequest(parameters);
        return (JSONObject) parameters.get(SIMPLE_RESPONSE);
    }

    protected abstract void beforeRequest(Map<String, Object> map);
    protected abstract void request(Map<String, Object> map);
    protected abstract void afterRequest(Map<String, Object> map);

    @Override
    protected void start(Map<String, Object> map) throws Exception {

    }


    @Override
    public Map<String, Object> getInfomation(Map<String, Object> map) {
        return null;
    }


    @Override
    protected void init(Map<String, Object> map) throws Exception {
        super.init(map);
    }

    @Override
    protected void restart(Map<String, Object> map) throws Exception {
    }

    @Override
    protected void destroy(Map<String, Object> map) throws Exception {
        super.destroy(map);
    }
}
