package com.zzy.bean.utils;

import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractComponent;
import com.zzy.bean.component.AbstractServiceComponent;
import com.zzy.bean.component.ComponentController;
import com.zzy.bean.exception.MyException;
import com.zzy.core.ServiceCoreComponent;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ComponentUtils.java
 * @Description 组件工具类
 * @createTime 2022年04月12日 15:24:00
 */
public class ComponentUtils {

    /**
     * 根据string查询到需要的组件
     * @param componentNameStr
     * @param root
     * @return
     */
    public static AbstractComponent getComponentByString(String componentNameStr, ComponentController root) throws MyException {
        String[] split = componentNameStr.split("\\.");
        AbstractComponent component = getComponent(split, 0, root);
        if (component != null) {
            return component;
        }
        else {
            throw new MyException("未找到目标构件，请检查路径是否正确");
        }
    }

    private static AbstractComponent getComponent(String[] componentNameArray, int current, ComponentController currentNodeController) {
        if (current >= componentNameArray.length){
            return null;
        }
        String componentName = componentNameArray[current];
        AbstractComponent abstractComponent = currentNodeController.getComponentMap().get(componentName);
        if (abstractComponent instanceof AbstractServiceComponent){
            if (current == componentNameArray.length-1){
                // 找到目标构件
                return abstractComponent;
            }
            else {
                // 递归查询
                return getComponent(componentNameArray, current+1, ((AbstractServiceComponent) abstractComponent).getComponentController());
            }
        }
        else if (current == componentNameArray.length-1){
            // 找到目标构件
            return abstractComponent;
        }
        else {
            // 当前构件没有子构件
            return null;
        }
    }


}
