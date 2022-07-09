package com.zzy.mqtt.bean;

import io.netty.handler.codec.mqtt.MqttPublishMessage;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Message.java
 * @Description TODO
 * @createTime 2022年02月08日 09:07:00
 */
public class Message {

    private long lastSentTimeStamp;

    private int messageId;

    private MqttPublishMessage mqttMessage;

    public Message(MqttPublishMessage mqttPublishMessage) {
        this.messageId = mqttPublishMessage.variableHeader().packetId();
        this.mqttMessage = mqttPublishMessage.copy();
        updateTime();
    }

    public void updateTime(){
        lastSentTimeStamp = System.currentTimeMillis();
    }

    public MqttPublishMessage getMqttMessage() {
        return mqttMessage;
    }

    public void setMqttMessage(MqttPublishMessage mqttMessage) {
        this.mqttMessage = mqttMessage;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public long getLastSentTimeStamp() {
        return lastSentTimeStamp;
    }

    public void setLastSentTimeStamp(long lastSentTimeStamp) {
        this.lastSentTimeStamp = lastSentTimeStamp;
    }
}
