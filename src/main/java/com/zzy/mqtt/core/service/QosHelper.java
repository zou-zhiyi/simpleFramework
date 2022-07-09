package com.zzy.mqtt.core.service;

import com.zzy.mqtt.bean.Message;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttPublishMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName QosHelper.java
 * @Description 用于qos确认
 * @createTime 2022年02月21日 16:32:00
 */
public class QosHelper {
    private Logger logger = LoggerFactory.getLogger(QosHelper.class);

    private Map<Integer, Map<Channel, Message>> messageMap = new ConcurrentHashMap<>();

    /**
     * 将消息保存
     * @param mqttPublishMessage
     * @param channels
     */
    public void putMessage(MqttPublishMessage mqttPublishMessage, Set<Channel> channels){
        int messageId = mqttPublishMessage.variableHeader().packetId();
        Map<Channel,Message> channelMessageMap = messageMap.get(messageId);
        if (channelMessageMap == null){
            channelMessageMap = new ConcurrentHashMap<>();
            messageMap.put(messageId,channelMessageMap);
        }
        Message message = new Message(mqttPublishMessage);
        for (Channel channel : channels) {
            channelMessageMap.put(channel,message);
        }
        logger.debug("a message put, the message Id is {}",messageId);
    }

    /**
     * 删除，当收到一个消息体时执行
     * @param messageId
     * @param channel
     */
    public void removeMessage(int messageId, Channel channel){
        Map<Channel, Message> channelMessageMap = messageMap.get(messageId);
        if (channelMessageMap != null) {
            channelMessageMap.remove(channel);
            if (channelMessageMap.isEmpty()){
                messageMap.remove(messageId);
                logger.debug("a message is all removed, the message Id is {}",messageId);
            }
            else {
                logger.debug("a message is removed, the message Id is {}",messageId);
            }
        }
        else {
            logger.debug("the message and Channel Map is empty, the message Id is {}",messageId);
        }

    }

    /**
     * 删除，当客户端离线时执行
     * @param channel
     */
    public void removeMessage(Channel channel){
        for (Map.Entry<Integer, Map<Channel, Message>> map : messageMap.entrySet()){
            for (Map.Entry<Channel, Message> innerMap : map.getValue().entrySet()){
                if (innerMap.getKey().equals(channel)){
                    // 从映射中移除，并释放byteBuff
                    MqttPublishMessage mqttMessage = map.getValue().get(channel).getMqttMessage();
                    mqttMessage.release(mqttMessage.refCnt());
                    map.getValue().remove(channel);
                    logger.debug("a message is removed, the message Id is {}",map.getKey());
                }
            }
            if (map.getValue().isEmpty()){
                messageMap.remove(map.getKey());
                logger.debug("a message is all removed, the message Id is {}",map.getKey());
            }
        }
    }

    private void sendMessage(int messageId, Channel channel){
        Map<Channel, Message> channelMessageMap = messageMap.get(messageId);
        if (channelMessageMap != null){
            Message message = channelMessageMap.get(channel);
            channel.writeAndFlush(message.getMqttMessage().copy());
        }
        else {
            logger.error("the message do not exist, message Id is {}",messageId);
        }
    }

    /**
     * 检测当前消息是否超过一定时间段未收到ack，如果未收到， 重新发送数据
     */
    public void checkTimeStamp(){
        long currentTimeMillis = System.currentTimeMillis();
        for (Map.Entry<Integer, Map<Channel, Message>> map : messageMap.entrySet()){
            for (Map.Entry<Channel, Message> innerMap : map.getValue().entrySet()){
                Message value = innerMap.getValue();
                long lastSentTimeStamp = value.getLastSentTimeStamp();
                if (currentTimeMillis-lastSentTimeStamp > 30*1000){
                    sendMessage(value.getMessageId(), innerMap.getKey());
                }
            }
        }
    }

    /**
     * 检测当前消息是否超过一定时间段未收到ack，如果未收到， 重新发送数据
     * @param period
     */
    public void checkTimeStamp(long period){
        long currentTimeMillis = System.currentTimeMillis();
        for (Map.Entry<Integer, Map<Channel, Message>> map : messageMap.entrySet()){
            for (Map.Entry<Channel, Message> innerMap : map.getValue().entrySet()){
                Message value = innerMap.getValue();
                long lastSentTimeStamp = value.getLastSentTimeStamp();
                if (currentTimeMillis-lastSentTimeStamp > period){
                    sendMessage(value.getMessageId(), innerMap.getKey());
                }
            }
        }
    }

    public void clear(){
        for (Map.Entry<Integer, Map<Channel, Message>> map : messageMap.entrySet()){
            for (Map.Entry<Channel, Message> innerMap : map.getValue().entrySet()){
                Message value = innerMap.getValue();
                value.getMqttMessage().release();
            }
            map.getValue().clear();
        }
        messageMap.clear();
    }

}
