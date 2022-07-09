package com.zzy.bean.component;

import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.lifeCycle.AbstractLifeCycle;
import com.zzy.core.CoreManagerImpl;
import lombok.Data;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import static com.zzy.utils.Constances.*;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Component.java
 * @Description 抽象容器类型
 * @createTime 2022年02月09日 20:16:00
 */
@Data
public abstract class AbstractComponent extends AbstractLifeCycle {
    protected Map<String, Object> initMap = new ConcurrentHashMap<>(1);
    protected Map<String, Object> restartMap= new ConcurrentHashMap<>(1);
    protected Map<String, Object> destroyMap= new ConcurrentHashMap<>(1);
    protected String ComponentName;
    protected AbstractComponent parentComponent;
    protected Boolean isChangeable = true;

    public void init() throws Exception {
        init0(initMap);
    }

    public void restart() throws Exception {
        restart0(restartMap);
    }

    public void destroy() throws Exception {
        destroy0(destroyMap);
    }

    public AbstractComponent getParentComponent() {
        return parentComponent;
    }

    public void setParentComponent(AbstractComponent abstractComponent) {
        this.parentComponent = abstractComponent;
    }

    // 获取容器状态
    public JSONObject getComponentStatus() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",this.state.name());
        jsonObject.put("name", this.getComponentName());
        jsonObject.put("isChangeable",isChangeable);
        jsonObject.put(INIT_MAP,initMap);
        jsonObject.put(RESTART_MAP,restartMap);
        jsonObject.put(DESTROY_MAP,destroyMap);
        return jsonObject;
    }

    // 如果需要请求容器，调用这个方法，需要组件自己去实现, 暂时废弃
    public abstract Map<String,Object> getInfomation(Map<String, Object> request);

    @Override
    protected void init(Map<String, Object> map) throws Exception {
        Boolean isBean = (Boolean) map.getOrDefault(IS_BEAN, false);
        if (isBean) {
            CoreManagerImpl.getInstance().registBean(this);
        }
    }

    @Override
    protected void destroy(Map<String, Object> map) throws Exception {
        CoreManagerImpl.getInstance().cancelBean(this.getClass().getName());
    }
}
