package com.zzy.mqtt.client.callback;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MyMqttCallBack.java
 * @Description 方便使用一个回调函数来处理多个主题
 * @createTime 2022年03月15日 19:11:00
 */
public class MyMqttCallBack implements MqttCallback {

    Map<String, MqttCallback> callbackMap = new ConcurrentHashMap<>();

    @Override
    public void connectionLost(Throwable throwable) {

    }

    @Override
    public void messageArrived(String s, MqttMessage mqttMessage) throws Exception {
        MqttCallback mqttCallback = callbackMap.get(s);
        mqttCallback.messageArrived(s, mqttMessage);
    }

    @Override
    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {

    }

    public void addCallBack(String topic, MqttCallback mqttCallback) {
        callbackMap.put(topic, mqttCallback);
    }
}
