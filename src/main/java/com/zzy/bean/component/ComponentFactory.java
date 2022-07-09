package com.zzy.bean.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.exception.MyException;
import com.zzy.bean.lifeCycle.interfaces.LifeCycle;
import com.zzy.utils.MeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.zzy.utils.Constances.*;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ComponentFactory.java
 * @Description 节点组件工厂，用于创建组件
 * @createTime 2022年02月09日 20:25:00
 */
public class ComponentFactory {

    private static volatile ComponentFactory componentFactory;

    public static ComponentFactory getInstance(){
        if (componentFactory == null){
            synchronized (ComponentFactory.class) {
                if (componentFactory == null) {
                    componentFactory = new ComponentFactory();
                }
            }
        }
        return componentFactory;
    }

    private Logger logger = LoggerFactory.getLogger(ComponentFactory.class);

    /**
     * 传入Component的构造配置
     * @param configMap 构造配置项
     * @return 构造出的Component
     * @throws Exception
     */
    public AbstractComponent construct(JSONObject configMap) throws MyException {
        AbstractComponent component = null;
        if (configMap == null){
            throw new MyException("a component config do not exist, please check the config is right") ;
        }
        logger.debug("start to construct component, the config is {}",configMap);
        if (configMap.getString(CONFIG_FILE_PATH) != null){
            configMap = MeFileUtils.readJSONObject(configMap.getString(CONFIG_FILE_PATH));
        }
        String filePath = configMap.getString(FILE_PATH);
        if (filePath == null){
            filePath = "";
        }
        String packagePath = configMap.getString(PACKAGE_PATH);
        if (packagePath == null){
            throw new MyException("a component packagePath do not exist, please check the config is right") ;
        }
        try {
            LifeCycle lifeCycle = MeFileUtils.readModel(filePath, packagePath);
            component = (AbstractComponent) lifeCycle;
            component.setComponentName(((String) configMap.getOrDefault(COMPONENT_NAME, packagePath)));
            component.setDestroyMap((Map<String, Object>) configMap.getOrDefault(DESTROY_MAP, new ConcurrentHashMap<>(1)));
            component.setInitMap((Map<String, Object>) configMap.getOrDefault(INIT_MAP, new ConcurrentHashMap<>(1)));
            component.setRestartMap((Map<String, Object>) configMap.getOrDefault(RESTART_MAP, new ConcurrentHashMap<>(1)));
        } catch (Exception e) {
            logger.error("Component read error, the exception is {}", e.getMessage());
        }
        logger.info("construct success, the componentName is {}",component.getComponentName());
        if (component instanceof AbstractServiceComponent) {
            logger.info("it is a serviceComponent");
            AbstractServiceComponent serviceComponent = (AbstractServiceComponent) component;
            serviceComponent.setChildComponentJson(configMap.getJSONArray(CHILD_COMPONENTS));
            return serviceComponent;
        }

        return component;
    }

    /**
     * 传入一系列Component的构造配置
     * @param configMapList 构造配置项(一个列表)
     * @return 构造出的容器集合
     * @throws Exception
     */
    public List<AbstractComponent> construct(JSONArray configMapList) throws MyException {
        List<AbstractComponent> abstractComponentList = new ArrayList<>();
        if (configMapList == null){
            return abstractComponentList;
        }
        int size = configMapList.size();
        for (int i = 0; i < size; i++) {
            abstractComponentList.add(construct(configMapList.getJSONObject(i)));
        }
        return abstractComponentList;
    }
}
