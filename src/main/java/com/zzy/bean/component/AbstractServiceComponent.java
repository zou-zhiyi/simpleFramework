package com.zzy.bean.component;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.exception.MyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName AbstractServerComponent.java
 * @Description 相比普通的容器，多了一个容器管理的内部类
 * @createTime 2022年02月11日 16:32:00
 */
public abstract class AbstractServiceComponent extends AbstractComponent{
    private Logger logger = LoggerFactory.getLogger(AbstractServiceComponent.class);

    private JSONArray childComponentJson;

    protected ComponentController componentController = new ComponentController(this);

    public ComponentController getComponentController() {
        return componentController;
    }

    public void setComponentController(ComponentController componentController) {
        this.componentController = componentController;
    }

    public JSONArray getChildComponentJson() {
        return childComponentJson;
    }


    @Override
    public JSONObject getComponentStatus(){
        JSONObject componentStatus = super.getComponentStatus();
        componentStatus.put("childComponents",componentController.getChildComponentStatus());
        return componentStatus;
    }


    public void setChildComponentJson(JSONArray childComponentJson) {
        this.childComponentJson = childComponentJson;
    }

    public void registChildComponents(){
        try {
            componentController.registComponent(childComponentJson);
        } catch (MyException e) {
            logger.error("component regist failed, the service component is {}, the error is {}",
                    getComponentName(), e.getMessage());
        }
    }

    public void registComponent(JSONObject componentRegist) {
        try {
            componentController.registComponent(componentRegist);
        } catch (MyException e) {
            logger.error("component regist failed, the service component is {}, the error is {}",
                    getComponentName(), e.getMessage());
        }
    }

    public void registChildComponents(JSONArray componentRegistList){
        try {
            componentController.registComponent(componentRegistList);
        } catch (MyException e) {
            logger.error("component regist failed, the service component is {}, the error is {}",
                    getComponentName(), e.getMessage());
        }
    }

    @Override
    public void init() throws Exception {
        componentController.initComponents();
        super.init();
    }

    @Override
    public void destroy() throws Exception {
        componentController.destroyComponents();
        super.destroy();
    }

}
