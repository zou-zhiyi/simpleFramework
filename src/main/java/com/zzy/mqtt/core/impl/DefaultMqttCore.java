package com.zzy.mqtt.core.impl;

import com.zzy.mqtt.core.service.MqttClient;
import com.zzy.mqtt.core.service.MqttClientController;
import com.zzy.mqtt.core.service.QosHelper;
import com.zzy.mqtt.core.SimpleMqttCore;
import com.zzy.mqtt.topic.TopicLevelController;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;


/**
 * @author admin
 * @version 1.0.0
 * @ClassName MessageQueue.java
 * @Description mqtt的消息队列，实现了订阅、发布功能，但是只实现了qos0和qos1级别的功能
 * @createTime 2022年02月02日 21:03:00
 */
public class DefaultMqttCore extends SimpleMqttCore {

    private Logger logger = LoggerFactory.getLogger(DefaultMqttCore.class);

    // 控制注册到服务器上的mqtt客户端
    private MqttClientController mqttClientController = new MqttClientController();

    // 用于进行主题匹配
    private TopicLevelController topicLevelController = new TopicLevelController();

    // 用于执行定时任务
    Timer clientTimer;

    // 用于qos的发布确认（暂时只做了qos为1）
    private QosHelper qosHelper = new QosHelper();

    @Override
    protected void connack(Channel channel, MqttMessage mqttMessage) throws Exception {
        logger.info("a client connect --: "+mqttMessage.toString());
        mqttClientController.addMqttClient(channel,mqttMessage);
    }

    @Override
    protected void puback(Channel channel, MqttMessage mqttMessage) throws Exception {
        logger.info("发布了消息");
        updateClientTime(channel);
        MqttPublishMessage mqttPublishMessage = ((MqttPublishMessage) mqttMessage);
        String topicName = mqttPublishMessage.variableHeader().topicName();
        MqttQoS mqttQoS = mqttPublishMessage.fixedHeader().qosLevel();
        Set<String> topicMatchingResult = topicLevelController.matchingTopic(topicName);
        logger.debug("publish topicMatchingResult is {},matching result is {}", topicName, topicMatchingResult);

        switch (mqttQoS){
            case AT_MOST_ONCE:
                publishMessageQos0(topicMatchingResult, mqttPublishMessage);
                break;
            case AT_LEAST_ONCE:
                // qos为1时，将message加入的映射， 然后再发布
                publishMessageQos1(topicMatchingResult, mqttPublishMessage);
            case EXACTLY_ONCE:
                // 暂未实现qos2
                break;
            default:
                break;
        }

    }

    @Override
    protected void pubackQos1(Channel channel, MqttMessage mqttMessage) throws Exception {
        MqttPubAckMessage mqttPubAckMessage = (MqttPubAckMessage) mqttMessage;
        int messageId =mqttPubAckMessage.variableHeader().messageId();
        // 删除目标message
        qosHelper.removeMessage(messageId, channel);
    }

    @Override
    protected void pubcomp(Channel channel, MqttMessage mqttMessage) throws Exception {

    }


    @Override
    protected void suback(Channel channel, MqttMessage mqttMessage) throws Exception {
        updateClientTime(channel);
        List<MqttTopicSubscription> mqttTopicSubscriptions
                = ((MqttSubscribeMessage) mqttMessage).payload().topicSubscriptions();
        for (MqttTopicSubscription topic:mqttTopicSubscriptions){
            // 将主题添加到构造树中,同时添加订阅主题到mqtt客户端类中
            topicLevelController.addSubTopic(topic.topicName());
            mqttClientController.subscribeTopic(topic, channel);
        }
    }

    @Override
    protected void unsuback(Channel channel, MqttMessage mqttMessage) throws Exception {
        updateClientTime(channel);
        Set<String> topics = ((MqttUnsubscribeMessage)mqttMessage).payload().topics().stream().collect(Collectors.toSet());
        unSubscirbeTopics(topics, channel);
    }

    @Override
    protected void pingresp(Channel channel, MqttMessage mqttMessage) throws Exception {
        updateClientTime(channel);
    }

    @Override
    protected void disconnect(Channel channel, MqttMessage mqttMessage) throws Exception {
        logger.info("a client disconnect --: {}",mqttMessage.toString());
        Set<String> topics = mqttClientController.getMqttClientMap().get(channel).getTopicMap().keySet();
        unSubscirbeTopics(topics, channel);
        mqttClientController.removeMqttClient(channel);
        qosHelper.removeMessage(channel);
    }

    @Override
    protected void init(Map<String, Object> map) throws Exception {
        // 定时器，用于判断客户端是否在线
        clientTimer = new Timer();
        int period = (int) map.getOrDefault("period",30);
        //  用定时器定时检测是否有异常离线的mqttClient
        clientTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                long currentTimeMillis = System.currentTimeMillis();
                Map<Channel, MqttClient> mqttClientMap = mqttClientController.getMqttClientMap();
                for (Map.Entry<Channel, MqttClient> mqttClientEntry : mqttClientMap.entrySet()){
                    MqttClient mqttClient = mqttClientEntry.getValue();
                    long temp = currentTimeMillis - mqttClient.getLastPingTime();
                    logger.info("时间差为--: {}, 时间间隔为--: {}",temp,mqttClient.getKeepAliveTimeSeconds()*3000);
                    // 定时确认时间戳
                    qosHelper.checkTimeStamp();
                    // 超过预定时间三倍，那么就注销掉客户端
                    if (temp>mqttClient.getKeepAliveTimeSeconds()*3*1000){
                        Channel channel = mqttClientEntry.getKey();
                        logger.info("a error client was removed, the clientId is {}",mqttClient.getClientId());
                        unSubscirbeTopics(mqttClientMap.get(channel).getTopicMap().keySet(), channel);
                        // 注销客户端
                        mqttClientMap.remove(channel);
                        // 将消息从qos中删除
                        qosHelper.removeMessage(channel);
                    }
                }
            }
        },0, period*1000);

//        DefaultHandlerMapping component = (DefaultHandlerMapping) ServiceCoreComponent.getInstance().getGeneralComponentController().getComponent(DEFAULT_HANDLER_MAPPING);
//        MessageHandler messageHandler = new MessageHandler();
//        messageHandler.setMethod("GET");
//        messageHandler.setUrl("/test");
//        component.registHandler(messageHandler.getMethodAndUrl() ,messageHandler);
    }

    @Override
    protected void restart(Map<String, Object> map) throws Exception {

    }

    @Override
    protected void destroy(Map<String, Object> map) throws Exception {
        mqttClientController.clear();
        topicLevelController.clear();
        qosHelper.clear();
        clientTimer.cancel();
        clientTimer = null;
    }

    /**
     * 通过channel确定mqtt客户端，更新其时间戳
     * @param channel
     */
    private void updateClientTime(Channel channel){
        mqttClientController.updateClientTime(channel);
    }

    private void unSubscirbeTopics(Set<String> topics, Channel channel){
        for (String topic:topics){
            topicLevelController.deleteTopic(topic);
            mqttClientController.unSubscribeTopic(topic, channel);
        }
    }


    private void publishMessageQos0(Set<String> topicMatchingResult, MqttPublishMessage mqttPublishMessage) {
        mqttClientController.publishMessageQos0(topicMatchingResult, mqttPublishMessage);
    }

    private void publishMessageQos1(Set<String> topicMatchingResult, MqttPublishMessage mqttPublishMessage) {
        Map<String, Set<Channel>> channelMap = mqttClientController.getChannelMap();
        for (String matchingResult:topicMatchingResult){
            Set<Channel> channels = channelMap.get(matchingResult);
            if (channels == null){
                return;
            }
            qosHelper.putMessage(mqttPublishMessage, channels);
            // 可能存在多次发送数据,需要提高其引用计数
            for (Channel channel0:channels){

                mqttPublishMessage.retain();
                channel0.writeAndFlush(mqttPublishMessage);
            }
        }
    }


    @Override
    public Map<String, Object> getInfomation(Map<String, Object> map) {
        return null;
    }

}
