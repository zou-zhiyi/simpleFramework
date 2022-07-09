package com.zzy.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractComponent;
import com.zzy.http.request.MultiRequestHandler;
import com.zzy.http.request.interfaces.RequestHandler;
import com.zzy.http.request.SingleRequestHandler;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.zzy.http.utils.HttpConstances.*;


/**
 * @author admin
 * @version 1.0.0
 * @ClassName HandlerMapping.java
 * @Description 将配置文件中的请求路径和对应的处理类绑定, 用于处理http请求
 * @createTime 2022年02月23日 10:40:00
 */
public class DefaultHttpHandlerMapping extends AbstractHandlerMapping {


    public DefaultHttpHandlerMapping() {
        setComponentName(DEFAULT_HANDLER_MAPPING);
    }

    private Logger logger = LoggerFactory.getLogger(DefaultHttpHandlerMapping.class);

    @Override
    public JSONObject doRequest(Map<String, Object> extendParameters, Map<String, Object> requestParameters){
        FullHttpRequest fullHttpRequest = (FullHttpRequest) extendParameters.get(HTTP_FULL_REQUEST);
        Channel channel = (Channel) extendParameters.get(HTTP_CHANNEL);
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(fullHttpRequest.uri());
        String uri = queryStringDecoder.rawPath();
        String methodName = fullHttpRequest.method().name();

        // 将两个parameter中的内容抽取成一个
        Map<String, Object> map = new HashMap<>();
        map.put(HTTP_HEADER, fullHttpRequest.headers());
        map.put(HTTP_CONTENT, fullHttpRequest.content());
        map.put(SIMPLE_PARAMETERS, requestParameters);
        map.put(URI, methodName.toUpperCase()+":"+uri);
        map.put(HTTP_VERSION, fullHttpRequest.protocolVersion());
        map.put(SIMPLE_RESPONSE, new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.BAD_REQUEST));

        // 搜寻对应的handler进行处理
        RequestHandler requestHandler = handlerMap.get(methodName + ":" + uri);
        if (requestHandler != null){
            try {
                JSONObject jsonObject = requestHandler.doRequest(map);
                logger.debug("request success, the path is {}", methodName+":"+uri);
                DefaultFullHttpResponse defaultFullHttpResponse =
                        new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.OK,
                                Unpooled.copiedBuffer(jsonObject.toJSONString().getBytes()));
                channel.writeAndFlush(defaultFullHttpResponse).addListener(ChannelFutureListener.CLOSE);
            } catch (Exception e) {
                logger.error("request error, the error is {}",e.getMessage());
                e.printStackTrace();
                DefaultFullHttpResponse defaultHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
                channel.writeAndFlush(defaultHttpResponse).addListener(ChannelFutureListener.CLOSE);
            }
        }
        else {
            logger.error("do not found request, the path is {}", methodName+":"+uri);
            DefaultFullHttpResponse defaultHttpResponse = new DefaultFullHttpResponse(HttpVersion.HTTP_1_0, HttpResponseStatus.NOT_FOUND);
            channel.writeAndFlush(defaultHttpResponse).addListener(ChannelFutureListener.CLOSE);
        }
        return null;
    }

    @Override
    public Map<String, Object> getInfomation(Map<String, Object> map) {
        return null;
    }

    @Override
    protected void init(Map<String, Object> map) throws Exception {
        super.init(map);
        for (Map.Entry<String, AbstractComponent> componentEntry
                : componentController.getComponentMap().entrySet()){

            // 需要改为instance of判断类型
            AbstractComponent value = componentEntry.getValue();
            // 单维处理handler
            if (value instanceof SingleRequestHandler){
                SingleRequestHandler singleRequestHandler = (SingleRequestHandler) value;
                handlerMap.put(singleRequestHandler.getMethodAndUrl(), singleRequestHandler);
            }
            // 多维处理handler
            else if (value instanceof MultiRequestHandler){
                MultiRequestHandler multiRequestHandler = (MultiRequestHandler) value;
                List<String> methodAndUrlList = multiRequestHandler.getMethodAndUrlList();
                for (String temp : methodAndUrlList) {
                    handlerMap.put(temp, multiRequestHandler);
                }
            }
        }
    }

    @Override
    protected void start(Map<String, Object> map) throws Exception {


    }

//    @Override
//    protected void start(Map<String, Object> map) throws Exception {
//
//        String handlerType = (String) map.get(HANDLER_TYPE);
//        switch (handlerType) {
//            case HANDLER_REQUEST:
//                Channel channel = (Channel) map.get(HTTP_CHANNEL);
//                FullHttpRequest fullHttpRequest = (FullHttpRequest) map.get(HTTP_FULL_REQUEST);
//                Map<String, Object> parameters = (Map<String, Object>) map.get(HTTP_PARAMETERS);
//                doRequest(channel, fullHttpRequest, parameters);
//                break;
//            case HANDLER_REG:
//                String methodAndUrl = (String) map.get(METHOD_URL);
//                AbstractComponent handlerComponent = (AbstractComponent) map.get(HANDLER_COMPONENT);
//                registHandler(methodAndUrl, handlerComponent);
//                break;
//            default:
//                logger.error("do not match handler mapping");
//                break;
//        }
//
//
//    }

    @Override
    protected void restart(Map<String, Object> map) throws Exception {

    }
}
