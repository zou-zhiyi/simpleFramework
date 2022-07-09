package com.zzy.rpc.server.loadbalance;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zzy.core.interfaces.BeanController;
import io.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static com.zzy.utils.Constances.BEAN_NAME;
import static com.zzy.utils.Constances.HOST_ID;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName ClusterBeanController.java
 * @Description 用于注册和获取对应的bean实例，由于多个主机可能拥有相同的实例，所以需要做负载均衡
 * @createTime 2022年04月19日 19:22:00
 */
public class ClusterBeanController implements BeanController {
    private Logger logger = LoggerFactory.getLogger(ClusterBeanController.class);

    // 由HostBalance决定使用哪个channel执行rpc请求
    private Map<String, HostBalance> registMap = new ConcurrentHashMap<>();

    // 客户端映射
    private Map<String, Host> hostMap = new ConcurrentHashMap<>();
    private Map<Channel, String> channelMap = new ConcurrentHashMap<>();

    /**
     * 版本检查，如果不是最新版本，更新数据
     * @param key
     * @param version
     * @param channel
     * @return
     */
    public boolean checkVersion(String key, long version, Channel channel) {
        Host host = hostMap.get(key);
        // 第一次注册，或是版本不匹配
        if (host == null) {
            logger.debug("client first connect");
            Host newHost = new Host();
            newHost.setHostId(key);
            newHost.setVersion(version);
            newHost.setChannel(channel);
            newHost.setBeanList(new CopyOnWriteArrayList<>());
            hostMap.put(key, newHost);
            channelMap.put(channel,key);
            return false;
        }
        else if (host.getVersion() != version) {
            logger.debug("version is not the newest, need update");
            host.setVersion(version);
            cleanHostBean(host);
        }
        return true;
//
//        Object host = hostMap.get(key);
//        // 第一次注册，或是版本不匹配
//        if (host == null) {
//            logger.debug("client first connect");
//            Host newHost = new Host();
//            newHost.setHostId(key);
//            newHost.setVersion(version);
//            newHost.setChannel(channel);
//            newHost.setBeanList(new CopyOnWriteArrayList<>());
//            hostMap.put(key, newHost);
//            return false;
//        }
//        else {
//            Host tempHost = (Host)host;
//            if (tempHost.getVersion() != version) {
//                logger.debug("version is not the newest, need update");
//                tempHost.setVersion(version);
//                cleanHostBean(tempHost);
//            }
//        }
//        return true;
    }

    @Override
    public void registBean(Object bean) {
        if (bean instanceof JSONObject) {
            logger.debug("regist json is {}",((JSONObject)bean).toJSONString());
            JSONObject jsonObject = (JSONObject) bean;
            String beanName = jsonObject.getString(BEAN_NAME);
            String hostId = jsonObject.getString(HOST_ID);
            Host host = hostMap.get(hostId);
            host.getBeanList().add(beanName);

            HostBalance hostBalance = registMap.get(beanName);
            if (hostBalance == null) {
                hostBalance = new HostBalance();
                registMap.put(beanName, hostBalance);
            }
            hostBalance.addHost(host);
        }
    }

    /**
     * 删除指定全部实例映射
     * @param className
     */
    @Override
    public void cancelBean(String className) {
        registMap.remove(className);
    }

    /**
     * 将原本host的bean实例映射删除
     * @param host
     */
    public void cleanHostBean(Host host) {
        if (host == null) {
            return;
        }
        List<String> beanList = host.getBeanList();
        for (String beanName : beanList) {
            HostBalance hostBalance = registMap.get(beanName);
            hostBalance.removeHost(host);
            logger.debug("hostBalance canceled host:{}, bean: {}", host.getHostId(), beanName);
        }
        host.getBeanList().clear();
    }

    public void cancelHostByChannel(Channel channel) {
        String hostId = channelMap.get(channel);
        if (hostId != null) {
            logger.debug("a host cancel:{}", hostId);
            Host host = hostMap.get(hostId);
            cleanHostBean(host);
            hostMap.remove(hostId);
            logger.debug("now host values is: {}", JSON.toJSONString(hostMap.keySet()));
        }
        channelMap.remove(channel);
    }

    @Override
    public List<String> getBeanList() {
        return new ArrayList<>(registMap.keySet());
    }

    /**
     * 获取一个拥有bean实例的客户端，可做负载均衡
     * @param className
     * @return
     */
    @Override
    public Object getBean(String className) {
        return registMap.get(className).getHost().getChannel();
    }
}
