package com.zzy.http.handler;

import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractComponent;
import com.zzy.http.request.MultiRequestHandler;
import com.zzy.http.request.interfaces.RequestHandler;
import com.zzy.http.request.SingleRequestHandler;
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
 * @Description 将配置文件中的请求路径和对应的处理类绑定, 以uri+method为key
 * @createTime 2022年02月23日 10:40:00
 */
public class DefaultHandlerMapping extends AbstractHandlerMapping {


    public DefaultHandlerMapping() {
        setComponentName(DEFAULT_HANDLER_MAPPING);
    }

    private Logger logger = LoggerFactory.getLogger(DefaultHandlerMapping.class);

    @Override
    public JSONObject doRequest(Map<String, Object> extendParameters, Map<String, Object> requestParameters){

        String uri = (String) extendParameters.get(URI);
        String methodName = (String) extendParameters.get(METHOD);
        Map<String, Object> map = new HashMap<>();
        map.put(URI, methodName.toUpperCase()+":"+uri);
        map.put("requestParameter", requestParameters);
        // 搜寻对应的handler进行处理
        RequestHandler requestHandler = handlerMap.get(methodName.toUpperCase() + ":" + uri);
        if (requestHandler != null){
            try {
                JSONObject jsonObject = requestHandler.doRequest(map);
                logger.debug("request success, the path is {}", methodName+":"+uri);
                return jsonObject;
            } catch (Exception e) {
                logger.error("request error, the error is {}",e.getMessage());
                e.printStackTrace();
                return null;
            }
        }
        else {
            logger.error("do not found request, the path is {}", methodName+":"+uri);
            return null;
        }
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
