package com.zzy.core;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractComponent;
import com.zzy.bean.exception.MyException;
import com.zzy.core.bean.ConfigRequest;
import com.zzy.core.bean.ControlRequest;
import com.zzy.core.bean.StatusRequest;
import com.zzy.core.interfaces.BeanController;
import com.zzy.core.interfaces.CoreManager;
import com.zzy.utils.MeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static com.zzy.core.ServiceCoreComponent.SERVICE_CORE_PATH;
import static com.zzy.utils.Constances.HOST_ID;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName CoreManager.java
 * @Description TODO
 * @createTime 2022年04月13日 15:22:00
 */
public class CoreManagerImpl implements CoreManager, BeanController {
    private static Logger logger = LoggerFactory.getLogger(CoreManagerImpl.class);

    private static volatile CoreManagerImpl coreManagerImpl = new CoreManagerImpl();

    // 主机唯一识别Id，uuid的string，第一次执行代码时生成
    private String hostId;

    // 用于标记版本号， bean内容发生改变时， 更新版本
    private volatile long version = System.currentTimeMillis();

    public static CoreManagerImpl getInstance() {
        return coreManagerImpl;
    }

//    public static CoreManagerImpl getInstance() {
//        if (coreManagerImpl == null) {
//            synchronized (CoreManagerImpl.class) {
//                if (coreManagerImpl == null) {
//                    coreManagerImpl ;
//                }
//            }
//        }
//        return coreManagerImpl;
//    }

    private ServiceCoreComponent coreComponent;


    public CoreManagerImpl() {
        registBean(this);
        logger.info(JSON.toJSONString(getBeanList()));
    }
    // 注册容器
    private Map<String, Object> beanMap = new ConcurrentHashMap<>();

    public void setCoreComponent(ServiceCoreComponent coreComponent) {
        this.coreComponent = coreComponent;
    }

    public CoreManagerImpl(ServiceCoreComponent coreComponent) {
        this.coreComponent = coreComponent;
    }

    @Override
    public void registBean(Object bean) {
        if (bean == null) {
            return;
        }
        String name = bean.getClass().getName();
        logger.info("a bean regist: {}",name);
        beanMap.put(name, bean);
        setVersion(System.currentTimeMillis());
    }

    @Override
    public List<String> getBeanList() {
        return new ArrayList<>(beanMap.keySet());
    }

    @Override
    public void cancelBean(String className) {
        logger.info("a bean cancel: {}", className);
        beanMap.remove(className);
        setVersion(System.currentTimeMillis());
    }

    @Override
    public Object getBean(String className) {
        return beanMap.get(className);
    }

    @Override
    public JSONObject getComponentStatus(Boolean isRoot, String componentStr) throws MyException {
        if (isRoot) {
            return getComponentStatus();
        }
        else {
            return getComponentStatus(componentStr);
        }
    }

    public JSONObject getComponentStatus(StatusRequest statusRequest) throws MyException {
        if (statusRequest.getIsRoot()) {
            return getComponentStatus();
        }
        else {
            return getComponentStatus(statusRequest.getComponentStr());
        }
    }

    /**
     * 获取根构件状态
     * @return
     * @throws MyException
     */
    private JSONObject getComponentStatus() throws MyException {
        return coreComponent.getComponentStatus();
    }

    /**
     * 获取指定构件状态
     * @param componentStr
     * @return
     * @throws MyException
     */
    public JSONObject getComponentStatus(String componentStr) throws MyException {
        AbstractComponent componentByString = coreComponent.getComponentByString(componentStr);
        return componentByString.getComponentStatus();
    }

    /**
     * 用于控制容器的启动、停止
     * @param componentStr
     * @param execution
     * @throws Exception
     */
    @Override
    public Boolean controlMsgExecute(String componentStr, String execution) throws Exception {
        AbstractComponent targetComponent = coreComponent.getComponentByString(componentStr);
        switch (execution) {
            case "init":
                targetComponent.init();
                return true;
            case "destroy":
                targetComponent.destroy();
                return false;
            default:
                throw new MyException("UnKnown execution");
        }
    }

    public Boolean controlMsgExecute(ControlRequest controlRequest) throws Exception {
        String componentStr = controlRequest.getComponentStr();
        AbstractComponent targetComponent = coreComponent.getComponentByString(componentStr);
        switch (controlRequest.getExecution()) {
            case "init":
                targetComponent.init();
                return true;
            case "destroy":
                targetComponent.destroy();
                return false;
            default:
                throw new MyException("UnKnown execution");
        }
    }

    /**
     * 用于修改配置项
     * @param configBody
     */
    @Override
    public Boolean configChange(JSONObject configBody) throws Exception {
        JSONObject tempJsonObject = MeFileUtils.readJSONObject(SERVICE_CORE_PATH);
        try {
            coreComponent.destroy();
            coreComponent.unRegistAllComponent();
            coreComponent.init0(configBody);
            MeFileUtils.wirteJSONObject(SERVICE_CORE_PATH, configBody);
            return true;
        } catch (Exception e) {
            try {
                coreComponent.init0(tempJsonObject);
            } catch (Exception exception) {
                throw exception;
            }
            throw e;
        }
    }

    public Boolean configChange(ConfigRequest configRequest) throws Exception {
        JSONObject tempJsonObject = MeFileUtils.readJSONObject(SERVICE_CORE_PATH);
        try {
            coreComponent.destroy();
            coreComponent.unRegistAllComponent();
            coreComponent.init0(configRequest.getConfigBody());
            MeFileUtils.wirteJSONObject(SERVICE_CORE_PATH, configRequest.getConfigBody());
            return true;
        } catch (Exception e) {
            try {
                coreComponent.init0(tempJsonObject);
            } catch (Exception exception) {
                throw exception;
            }
            throw e;
        }
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    public String getHostId() {
        return hostId;
    }

    public void setHostId(String hostId) {
        this.hostId = hostId;
    }
}
