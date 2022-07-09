package com.zzy.mqtt.client;

import com.zzy.bean.component.AbstractComponent;
import com.zzy.mqtt.client.callback.MyMqttCallBack;
import lombok.Data;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MqttClient.java
 * @Description 用于连接mqtt服务端，并发布数据的client
 * @createTime 2022年03月15日 09:59:00
 */
@Data
public class MyMqttClient {
    private Logger logger = LoggerFactory.getLogger(MyMqttClient.class);
    private MqttClient client;
    private MqttCallback myMqttCallBack;
    Map<String, AbstractComponent> componentMap = new ConcurrentHashMap<>();

    public MyMqttClient(){

    }

    public MyMqttClient(String broker, String clientId) {
        MemoryPersistence memoryPersistence = new MemoryPersistence();
        try {
            client = new MqttClient(broker, clientId, memoryPersistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("");
            options.setPassword("".toCharArray());
            options.setCleanSession(false);
            myMqttCallBack = new MyMqttCallBack();
            client.setCallback(myMqttCallBack);
            client.connect(options);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public MyMqttClient(String broker, String clientId, MqttCallback mqttCallback) {
        MemoryPersistence memoryPersistence = new MemoryPersistence();
        try {
            client = new MqttClient(broker, clientId, memoryPersistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("");
            options.setPassword("".toCharArray());
            options.setCleanSession(false);
            myMqttCallBack = mqttCallback;
            client.setCallback(mqttCallback);
            client.connect(options);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void connack(String broker, String clientId) {
        MemoryPersistence memoryPersistence = new MemoryPersistence();
        try {
            client = new MqttClient(broker, clientId, memoryPersistence);
            MqttConnectOptions options = new MqttConnectOptions();
            options.setUserName("");
            options.setPassword("".toCharArray());
            options.setCleanSession(false);
            client.setCallback(myMqttCallBack);
            client.connect(options);

        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

}
