package com.zzy.http.utils;

import com.zzy.bean.component.AbstractComponent;
import com.zzy.bean.exception.MyException;
import com.zzy.core.ServiceCoreComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

import static com.zzy.http.utils.HttpConstances.*;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName HandlerUtils.java
 * @Description 用于将http请求容器注册到通用组件控制类下，暂时废弃
 * @createTime 2022年03月07日 22:11:00
 */
public class HandlerUtils {
    private static Logger logger = LoggerFactory.getLogger(HandlerUtils.class);

    /**
     * 将http请求容器注册到handlerMapping中
     * @param requestType 请求类型，get或post等
     * @param url         请求url
     * @param requestHandler 处理请求的handler
     * @throws Exception 抛出异常
     */
    public static void addHandlerToGeneralComponents(String requestType, String url , AbstractComponent requestHandler) throws Exception {
        AbstractComponent handlerMapping;
        try {
            handlerMapping = ServiceCoreComponent.getInstance()
                    .getGeneralComponentController().getComponent("HandlerMapping");
        } catch (Exception e) {
            logger.error("add new handler to genralComponentsfailed, the exception is {}", e.getMessage());
            return;
        }
        if (handlerMapping == null){
            throw new MyException("the handlerMapping component do not exist");
        }
        Map<String, Object> map = new HashMap<>();
        map.put(HANDLER_TYPE, HANDLER_REG);
        map.put(HANDLER_COMPONENT, requestHandler);
        map.put(METHOD_URL, requestType+":"+url);
        handlerMapping.start0(map);
    }
}
