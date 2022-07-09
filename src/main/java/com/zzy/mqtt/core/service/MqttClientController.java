package com.zzy.mqtt.core.service;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttConnectMessage;
import io.netty.handler.codec.mqtt.MqttMessage;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import io.netty.handler.codec.mqtt.MqttTopicSubscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MqttClientController.java
 * @Description 管理服务端连接上的mqtt客户端
 * @createTime 2022年02月22日 10:17:00
 */
public class MqttClientController {

    private Logger logger = LoggerFactory.getLogger(MqttClientController.class);

    // 存储对应的订阅集合,一个主题有多个订阅方
    private Map<String, Set<Channel>> channelMap = new ConcurrentHashMap<>();

    // 存储channel对应的一个mqtt客户端的映射
    private Map<Channel, MqttClient> mqttClientMap = new ConcurrentHashMap<>();


    public void addMqttClient(Channel channel, MqttMessage mqttMessage) {
        mqttClientMap.put(channel,new MqttClient(channel,((MqttConnectMessage)mqttMessage)));
        logger.info("mqttclientMap info --: {}",mqttClientMap);
    }

    public void removeMqttClient(Channel channel) {
        mqttClientMap.remove(channel);
        logger.info("mqttclient Map info --: {}", mqttClientMap);
    }

    /**
     * 发布消息，对所有订阅了的主机发布消息,等级为Qos0
     * @param topicMatchingResult
     * @param mqttPublishMessage
     */
    public void publishMessageQos0(Set<String> topicMatchingResult, MqttPublishMessage mqttPublishMessage){
        for (String matchingResult:topicMatchingResult){
            Set<Channel> channels = channelMap.get(matchingResult);
            if (channels == null){
                return;
            }
            // 可能存在多次发送数据,需要提高其引用计数
            for (Channel channel0:channels){

                mqttPublishMessage.retain();
                channel0.writeAndFlush(mqttPublishMessage);
            }
        }
    }

    /**
     * 发布消息，对所有订阅了的主机发布消息，等级为Qos1
     * @param topicMatchingResult
     * @param mqttPublishMessage
     */
    public void publishMessageQos1(Set<String> topicMatchingResult, MqttPublishMessage mqttPublishMessage){
        for (String matchingResult:topicMatchingResult){
            Set<Channel> channels = channelMap.get(matchingResult);
            if (channels == null){
                return;
            }
            // 可能存在多次发送数据,需要提高其引用计数
            for (Channel channel0:channels){

                mqttPublishMessage.retain();
                channel0.writeAndFlush(mqttPublishMessage);
            }
        }
    }

    /**
     * 订阅主题
     * @param topic
     * @param channel
     */
    public void subscribeTopic(MqttTopicSubscription topic, Channel channel) {
        MqttClient mqttClient = mqttClientMap.get(channel);
        mqttClient.addSubTopic(topic.topicName(),topic.qualityOfService());

        Set<Channel> channels = channelMap.get(topic.topicName());

        // 该订阅为新增订阅， 新建一个set集合
        if (channels == null){
            channels = new CopyOnWriteArraySet<>();
            channelMap.put(topic.topicName(),channels);
            channels.add(channel);
        }
        else {
            channels.add(channel);
        }
    }

    /**
     * 取消订阅
     * @param topic
     * @param channel
     */
    public void unSubscribeTopic(String topic, Channel channel) {
        Set<Channel> channels = channelMap.get(topic);
        if (channels == null){
            logger.error("the channel Map is null, please check");
        }
        else{
            // 将主题从构造树中移除，同时删除mqtt客户端中订阅的主题
            MqttClient mqttClient = mqttClientMap.get(channel);
            mqttClient.deleteSubTopic(topic);
            logger.debug("a client unSubscribe a topic, topic Name is {}, client Id is {}",topic,mqttClient.getClientId());
            channels.remove(channel);
        }

        // 如果当前没有订阅方，将其删除，节省内存
        if (channels.isEmpty()){
            channelMap.remove(topic);
        }
    }



    public void updateClientTime(Channel channel){
        MqttClient mqttClient = mqttClientMap.get(channel);
        if (mqttClient == null){
            logger.error("a mqttClient is null");
        }
        else {
            logger.info("updateLastPingTime : clientId is {}",mqttClient.getClientId());
            mqttClient.setLastPingTime(System.currentTimeMillis());
        }
    }

    public void clear() {
        channelMap.clear();
        mqttClientMap.clear();
    }

    public Map<Channel, MqttClient> getMqttClientMap() {
        return mqttClientMap;
    }

    public void setMqttClientMap(Map<Channel, MqttClient> mqttClientMap) {
        this.mqttClientMap = mqttClientMap;
    }

    public Map<String, Set<Channel>> getChannelMap() {
        return channelMap;
    }

    public void setChannelMap(Map<String, Set<Channel>> channelMap) {
        this.channelMap = channelMap;
    }
}
