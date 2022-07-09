package com.zzy.bean.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.exception.MyException;
import com.zzy.core.ServiceCoreComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ComponentController.java
 * @Description 用于控制容器
 * @createTime 2022年02月10日 22:34:00
 */
public class ComponentController {
    private AbstractComponent component;

    private Map<String, AbstractComponent> componentMap = new ConcurrentHashMap<>();

    private Logger logger = LoggerFactory.getLogger(ComponentController.class);

    public ComponentController(AbstractComponent component) {
        this.component = component;
    }

    public JSONArray getChildComponentStatus() {
        JSONArray jsonArray = new JSONArray();
        for (Map.Entry entry : componentMap.entrySet()){
            jsonArray.add(((AbstractComponent)entry.getValue()).getComponentStatus());
        }
        return jsonArray;
    }

    /**
     * 注册容器（一系列）
     * @param configMapList 配置项
     * @throws MyException
     */
    public void registComponent(JSONArray configMapList) throws MyException {
        List<AbstractComponent> abstractComponents = ComponentFactory.getInstance().construct(configMapList);
        for (AbstractComponent component: abstractComponents) {
            AbstractComponent put = componentMap.put(component.getComponentName(), component);
            if (put != null) {
                throw new MyException("ComponentName has exist: "+component.getComponentName());
            }
            // 如果为serviceComponent，调用controller初始化其子组件
            if (component instanceof AbstractServiceComponent){

                logger.info("a serviceComponent is registing, the componentName is {}", component.getComponentName());
                ((AbstractServiceComponent) component).registChildComponents();
                logger.info("a serviceComponent is registed, the componentName is {}", component.getComponentName());
            }
            // 设置其父组件
            component.setParentComponent(this.component);
        }
    }

    /**
     * 注册容器（单个）
     * @param configMap 配置项
     * @throws MyException
     */
    public void registComponent(JSONObject configMap) throws MyException {
        AbstractComponent component = ComponentFactory.getInstance().construct(configMap);
        AbstractComponent put = componentMap.put(component.getComponentName(), component);
        if (put != null) {
            throw new MyException("ComponentName has exist: "+component.getComponentName());
        }

        // 如果为核心服务容器， 初始化其子容器
        if (component instanceof AbstractServiceComponent){
            ((AbstractServiceComponent) component).registChildComponents();
        }
    }

    public void registComponent(AbstractComponent component) throws MyException {
        if (component.getComponentName() == null){
            logger.error("a component do not have a name, so can not be registed");
            return;
        }
        AbstractComponent put = componentMap.put(component.getComponentName(), component);
        if (put != null) {
            throw new MyException("ComponentName has exist: "+component.getComponentName());
        }

        // 如果为核心服务容器， 初始化其子容器
        if (component instanceof AbstractServiceComponent){
            ((AbstractServiceComponent) component).registChildComponents();
        }
    }

    public void unRegistAllComponent() {
        for (AbstractComponent abstractComponent : componentMap.values()){
            if (abstractComponent instanceof  AbstractServiceComponent) {
                AbstractServiceComponent serviceComponent = (AbstractServiceComponent) abstractComponent;
                serviceComponent.getComponentController().unRegistAllComponent();
            }
        }
        componentMap.clear();
    }

    /**
     * 获取目标容器
     * @param componentName 容器名
     * @return  容器
     * @throws Exception
     */
    public AbstractComponent getComponent(String componentName) throws Exception {
        AbstractComponent component = componentMap.get(componentName);
        if (component == null){
            throw new MyException("Component do not exists: "+componentName);
        }
        return component;
    }

    /**
     * 删除容器
     * @param componentName 容器名
     */
    public void deleteComponent(String componentName) {
        componentMap.remove(componentName);
    }

    /**
     * init所有容器
     */
    public void initComponents(){
        for (Map.Entry<String, AbstractComponent> entry:componentMap.entrySet()){
            try {
                logger.debug("Component start to init, the component name is {}", entry.getKey());
                entry.getValue().init();
                logger.debug("Component init success, the component name is {}", entry.getKey());
            } catch (Exception e) {
                logger.error("Component init failed, the component name is {}, the Exception is {}"
                        ,entry.getKey() ,e.getMessage());
            }
        }
    }

    /**
     * 选择性init容器
     * @param componentNames
     */
    public void initComponents(Set<String> componentNames){
        for (String componentName:componentNames){
            initComponent(componentName);
        }
    }

    /**
     * 选择性init容器
     * @param componentName
     */
    public void initComponent(String componentName){
        AbstractComponent abstractComponent = componentMap.get(componentName);
        if (abstractComponent == null){
            logger.error("Component init failed, the component name do not exists: {}",componentName);
        }
        else {
            try {
                abstractComponent.init();
            } catch (Exception e) {
                logger.error("Component init failed, the component name is {}",componentName);
            }
        }
    }

    /**
     * restart所有容器
     */
    public void restartComponents(){
        for (Map.Entry<String, AbstractComponent> entry:componentMap.entrySet()){
            try {
                entry.getValue().restart();
            } catch (Exception e) {
                logger.error("Component restart failed, the component name is {}",entry.getKey());
            }
        }
    }

    /**
     * 选择性restart容器
     * @param componentNames
     */
    public void restartComponents(Set<String> componentNames){
        for (String componentName:componentNames){
            restartComponent(componentName);
        }
    }

    /**
     * 选择性restart容器
     * @param componentName
     */
    public void restartComponent(String componentName){
        AbstractComponent abstractComponent = componentMap.get(componentName);
        if (abstractComponent == null){
            logger.error("Component restart failed, the component name do not exists: {}",componentName);
        }
        else {
            try {
                abstractComponent.init();
            } catch (Exception e) {
                logger.error("Component restart failed, the component name is {}",componentName);
            }
        }
    }

    /**
     * destroy容器
     */
    public void destroyComponents(){
        for (Map.Entry<String, AbstractComponent> entry:componentMap.entrySet()){
            try {
                entry.getValue().destroy();
            } catch (Exception e) {
                logger.error("Component destroy failed, the component name is {}",entry.getKey());
            }
        }
    }

    /**
     * 选择性destroy容器
     * @param componentNames
     */
    public void destroyComponents(Set<String> componentNames){
        for (String componentName:componentNames){
            destroyComponent(componentName);
        }
    }

    /**
     * 选择性destroy容器
     * @param componentName
     */
    public void destroyComponent(String componentName){
        AbstractComponent abstractComponent = componentMap.get(componentName);
        if (abstractComponent == null){
            logger.error("Component destroy failed, the component name do not exists: {}",componentName);
        }
        else {
            try {
                abstractComponent.destroy();
            } catch (Exception e) {
                logger.error("Component destroy failed, the component name is {}",componentName);
            }
        }
    }

    public Map<String, AbstractComponent> getComponentMap() {
        return componentMap;
    }

    public void setComponentMap(Map<String, AbstractComponent> componentMap) {
        this.componentMap = componentMap;
    }

    //    // 使用单例模式， 控制通用容器controller
//    private static ComponentController generalComponentController = new ComponentController();
//    public static ComponentController getInstance(){
//        return generalComponentController;
//    }
}
