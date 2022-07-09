package com.zzy.core;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.zzy.bean.component.AbstractComponent;
import com.zzy.bean.component.AbstractServiceComponent;
import com.zzy.bean.component.ComponentController;
import com.zzy.bean.exception.MyException;
import com.zzy.bean.utils.ComponentUtils;
import com.zzy.rpc.client.RpcNettyDistributeClient;
import com.zzy.rpc.server.LoadBalanceNettyServer;
import com.zzy.rpc.server.SimpleRPCNettyServer;
import com.zzy.utils.MeFileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import static com.zzy.utils.Constances.*;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ServiceCoreComponent.java
 * @Description 服务运行的核心容器
 * @createTime 2022年02月12日 16:03:00
 */
public class ServiceCoreComponent extends AbstractServiceComponent {
    public static final String GENERAL_COMPONENT = "generalComponent";
    public static final String SERVICE_COMPONENT = "serviceComponent";
    public static String SERVICE_CORE_PATH = "zzyPorject/config/nettyConfig.json";
    public static String CONFIG_PATH = "zzyPorject/config/ValueConfig.json";

    private Logger logger = LoggerFactory.getLogger(ServiceCoreComponent.class);
    // 用于存储通用容器
    private ComponentController generalComponentController = new ComponentController(this);

    // 处理rpc请求的服务器
    private SimpleRPCNettyServer rpcServer;

    // 分布式系统中，用于接收，注册所有实例的服务器，可代理rpc请求
    private LoadBalanceNettyServer loadBalanceNettyServer;

    // 分布式系统中，将自己的实例以心跳包的形式，注册到服务器上，同时接受服务器的rpc请求
    private RpcNettyDistributeClient distributeClient;

    private JSONObject config;

    @Override
    public Map<String, Object> getInfomation(Map<String, Object> request) {
        return null;
    }

    @Override
    public JSONObject getComponentStatus() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("status",this.state.name());
        jsonObject.put("name", this.getComponentName());
        jsonObject.put("isChangeable",false);
        jsonObject.put(INIT_MAP,initMap);
        jsonObject.put(RESTART_MAP,restartMap);
        jsonObject.put(DESTROY_MAP,destroyMap);
        JSONObject child1 = new JSONObject();
        JSONObject child2 = new JSONObject();
        JSONArray childComponentStatus = componentController.getChildComponentStatus();
        child1.put("name","serviceComponent");
        child1.put("childComponents",childComponentStatus);
        child1.put("isChangeable",false);
        JSONArray generalChildComponentStatus = generalComponentController.getChildComponentStatus();
        child2.put("name","generalComponent");
        child2.put("childComponents",generalChildComponentStatus);
        child1.put("isChangeable",false);
        JSONArray array = new JSONArray();
        array.add(child1);
        array.add(child2);
        jsonObject.put("childComponents",array);
        return jsonObject;
    }

    public AbstractComponent getComponentByString(String componentStr) throws MyException {
        if (componentStr == null) {
            throw new MyException("未输入路径，请检查");
        }
        String[] split = componentStr.split("\\.", 2);
        if (split.length != 2) {
            throw new MyException("请检查路径是否正确");
        }
        switch (split[0]) {
            case GENERAL_COMPONENT:
                return ComponentUtils.getComponentByString(split[1], this.generalComponentController);
            case SERVICE_COMPONENT:
                return ComponentUtils.getComponentByString(split[1], this.componentController);
            default:
                throw new MyException("未找到目标构件，请检查路径是否正确");

        }
    }

    @Override
    protected void init(Map<String, Object> map) throws Exception {
        // 注册服务型容器,并初始化
        super.init(map);

        logger.info("current file path is {}", SERVICE_CORE_PATH);
        if (map == null || map.isEmpty()){
            logger.info("the config file is empty");
            return;
        }
        // 注册通用容器
        JSONArray generalComponentConfig = (JSONArray) map.get(GENERAL_COMPONENT);

        generalComponentController.registComponent(generalComponentConfig);
        generalComponentController.initComponents();

        JSONArray serviceComponentConfig = (JSONArray) map.get(SERVICE_COMPONENT);
        componentController.registComponent(serviceComponentConfig);
        componentController.initComponents();

        // 启动rpc服务器
        JSONObject rpcConfig = (JSONObject) map.get(RPC_CONFIG);
        if (rpcConfig != null) {
            Integer rpcPort = rpcConfig.getInteger(PORT);
            rpcServer = new SimpleRPCNettyServer();
            rpcServer.start(rpcPort);
        }

        // 启动中央rpc服务器
        JSONObject rpcBalanceConfig = (JSONObject) map.get(RPC_LOAD_BALANCE_CONFIG);
        if (rpcBalanceConfig != null) {
            Integer rpcPort = rpcBalanceConfig.getInteger(PORT);
            loadBalanceNettyServer = new LoadBalanceNettyServer();
            loadBalanceNettyServer.start(rpcPort);
        }

        // 启动rpc客户端（分布式系统）
        JSONObject rpcDistributeConfig = (JSONObject) map.get(RPC_DISTRIBUTE_CONFIG);
        if (rpcDistributeConfig != null) {
            String rpcHost = rpcDistributeConfig.getString(HOST);
            Integer rpcPort = rpcDistributeConfig.getInteger(PORT);

            // 开启rpc客户端服务，然后启动心跳
            distributeClient = new RpcNettyDistributeClient(rpcHost, rpcPort);
            distributeClient.connect();
            distributeClient.heartBeat();
        }


    }

    @Override
    protected void start(Map<String, Object> map) throws Exception {

    }

    @Override
    protected void restart(Map<String, Object> map) throws Exception {
        generalComponentController.restartComponents();
        componentController.restartComponents();
    }

    @Override
    protected void destroy(Map<String, Object> map) throws Exception {
        super.destroy(map);
        generalComponentController.destroyComponents();
    }

    public void unRegistAllComponent() {
        generalComponentController.unRegistAllComponent();
        componentController.unRegistAllComponent();
    }

    public void serverStart() throws Exception {

        try {
            MeFileUtils.constructFiles(SERVICE_CORE_PATH);
        } catch (IOException e) {
            logger.debug("construct file and directory failed， maybe it has exists");
        }
        try {
            MeFileUtils.constructFiles(CONFIG_PATH);
        } catch (IOException e) {
            logger.debug("construct file and directory failed， maybe it has exists");
        }
        JSONObject jsonObject = MeFileUtils.readJSONObject(SERVICE_CORE_PATH);
        config = MeFileUtils.readJSONObject(CONFIG_PATH);
        String hostId = config.getString(HOST_ID);
        if (hostId == null) {
            hostId = UUID.randomUUID().toString();
            config.put(HOST_ID, hostId);
            MeFileUtils.wirteJSONObject(CONFIG_PATH, config);
        }
        CoreManagerImpl.getInstance().setHostId(hostId);
        CoreManagerImpl.getInstance().setCoreComponent(this);
        setComponentName("core");
        try {
            init0(jsonObject);
        } catch (Exception e) {
            logger.error("service Core Component error, the Exception is {}",e.getMessage());
        }
    }

    public void setNettyConfigPath(File file) {
        if (file.isDirectory()) {
            SERVICE_CORE_PATH = file.getAbsolutePath()+"/nettyConfig.json";
        }
    }

    public void setConfigPath(File file) {
        if (file.isDirectory()) {
            CONFIG_PATH = file.getAbsolutePath() + "/ValueConfig.json";
        }
    }

    public ComponentController getGeneralComponentController() {
        return generalComponentController;
    }

    public void setGeneralComponentController(ComponentController generalComponentController) {
        this.generalComponentController = generalComponentController;
    }

    private static ServiceCoreComponent serviceCoreComponent = new ServiceCoreComponent();

    public static ServiceCoreComponent getInstance(){
        return serviceCoreComponent;
    }

    public JSONObject getConfig() {
        return config;
    }

    public void setConfig(JSONObject config) {
        this.config = config;
    }


}
