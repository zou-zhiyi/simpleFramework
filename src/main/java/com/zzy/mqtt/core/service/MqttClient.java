package com.zzy.mqtt.core.service;

import com.zzy.mqtt.topic.Topic;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName PubLisher.java
 * @Description 发布方结构，用于mqtt服务端管理连接上的客户端
 * @createTime 2022年02月08日 09:20:00
 */
public class MqttClient {
    private Channel channel;
    private String clientId;
    private boolean isCleanSession;
    private boolean willFlag;
    private String willTopic;
    private byte[] willMessage;
    private MqttQoS willQos;
    private boolean willRetain;
    private boolean userNameFlag;
    private String userName;
    private boolean passwordFlag;
    private String password;
    private Map<String, Topic> topicMap;
    private int keepAliveTimeSeconds;
    private long lastPingTime;


    public MqttClient(Channel channel, MqttConnectMessage mqttConnectMessage) {
        this.channel = channel;
        MqttFixedHeader mqttFixedHeader = mqttConnectMessage.fixedHeader();
        MqttConnectPayload payload = mqttConnectMessage.payload();
        MqttConnectVariableHeader mqttConnectVariableHeader = mqttConnectMessage.variableHeader();
        clientId = payload.clientIdentifier();
        willFlag = mqttConnectVariableHeader.isWillFlag();
        isCleanSession = mqttConnectVariableHeader.isCleanSession();
        willRetain = mqttConnectVariableHeader.isWillRetain();
        userNameFlag = mqttConnectVariableHeader.hasUserName();
        passwordFlag = mqttConnectVariableHeader.hasPassword();
        keepAliveTimeSeconds = mqttConnectVariableHeader.keepAliveTimeSeconds();
        lastPingTime = System.currentTimeMillis();
        topicMap = new ConcurrentHashMap<>();
        if (willFlag){
            willQos = mqttFixedHeader.qosLevel();
            willTopic = payload.willTopic();
            willMessage = payload.willMessageInBytes();
        }
        if (userNameFlag){
            userName = payload.userName();
        }
        if (passwordFlag){
            password = payload.password();
        }
    }

    public void addSubTopic(String topicName, MqttQoS qoS){
        topicMap.put(topicName, new Topic(topicName, qoS));
    }

    public void deleteSubTopic(String topicName){
        topicMap.remove(topicName);
    }

    public Channel getChannel() {
        return channel;
    }

    public void setChannel(Channel channel) {
        this.channel = channel;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isCleanSession() {
        return isCleanSession;
    }

    public void setCleanSession(boolean cleanSession) {
        isCleanSession = cleanSession;
    }

    public boolean isWillFlag() {
        return willFlag;
    }

    public void setWillFlag(boolean willFlag) {
        this.willFlag = willFlag;
    }

    public MqttQoS getWillQos() {
        return willQos;
    }

    public void setWillQos(MqttQoS willQos) {
        this.willQos = willQos;
    }

    public boolean isWillRetain() {
        return willRetain;
    }

    public void setWillRetain(boolean willRetain) {
        this.willRetain = willRetain;
    }

    public boolean isUserNameFlag() {
        return userNameFlag;
    }

    public void setUserNameFlag(boolean userNameFlag) {
        this.userNameFlag = userNameFlag;
    }

    public boolean isPasswordFlag() {
        return passwordFlag;
    }

    public void setPasswordFlag(boolean passwordFlag) {
        this.passwordFlag = passwordFlag;
    }

    public Map<String, Topic> getTopicMap() {
        return topicMap;
    }

    public void setTopicMap(Map<String, Topic> topicMap) {
        this.topicMap = topicMap;
    }

    public String getWillTopic() {
        return willTopic;
    }

    public void setWillTopic(String willTopic) {
        this.willTopic = willTopic;
    }

    public byte[] getWillMessage() {
        return willMessage;
    }

    public void setWillMessage(byte[] willMessage) {
        this.willMessage = willMessage;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public long getLastPingTime() {
        return lastPingTime;
    }

    public void setLastPingTime(long lastPingTime) {
        this.lastPingTime = lastPingTime;
    }

    public int getKeepAliveTimeSeconds() {
        return keepAliveTimeSeconds;
    }

    public void setKeepAliveTimeSeconds(int keepAliveTimeSeconds) {
        this.keepAliveTimeSeconds = keepAliveTimeSeconds;
    }

}
