package com.zzy.mqtt.utils;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

import static com.zzy.mqtt.utils.MqttConstances.MQTT_CHANNEL;
import static com.zzy.mqtt.utils.MqttConstances.MQTT_MSG;


/**
 * @author admin
 * @version 1.0.0
 * @ClassName MqttMsgUtils.java
 * @Description 发布所有的mqtt确认数据报
 * @createTime 2022年01月26日 14:49:00
 */
public class MqttMsgUtils {



    private static Logger logger = LoggerFactory.getLogger(MqttMsgUtils.class);

    /**
     * 确认mqtt连接请求
     * @param channel
     * @param mqttMessage
     */
    public static void connack(Channel channel, MqttMessage mqttMessage){
        MqttConnectMessage mqttConnectMessage = (MqttConnectMessage) mqttMessage;
        MqttFixedHeader mqttFixedHeaderInfo = mqttConnectMessage.fixedHeader();
        MqttConnectVariableHeader mqttConnectVariableHeaderInfo = mqttConnectMessage.variableHeader();

        // 构建返回报文，可变报头
        MqttConnAckVariableHeader mqttConnAckVariableHeaderBack
                = new MqttConnAckVariableHeader(MqttConnectReturnCode.CONNECTION_ACCEPTED,
                mqttConnectVariableHeaderInfo.isCleanSession());
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack
                = new MqttFixedHeader(MqttMessageType.CONNACK,mqttFixedHeaderInfo.isDup(),
                MqttQoS.AT_MOST_ONCE,mqttFixedHeaderInfo.isRetain(),0x02);
        // 构建CONNACK消息体
        MqttConnAckMessage connAck = new MqttConnAckMessage(mqttFixedHeaderBack,mqttConnAckVariableHeaderBack);
        logger.info("message connack back--: "+connAck.toString());
        channel.writeAndFlush(connAck);
    }

    /**
     * 根据qos发布确认
     * @param channel
     * @param mqttMessage
     */
    public static void puback(Channel channel, MqttMessage mqttMessage){
        MqttPublishMessage mqttPublishMessage = (MqttPublishMessage) mqttMessage;
        MqttFixedHeader mqttFixedHeaderInfo = mqttPublishMessage.fixedHeader();
        MqttQoS qos = mqttFixedHeaderInfo.qosLevel();
        logger.debug("current mqttPublishMessage count is {}",mqttPublishMessage.refCnt());

        byte[] headBytes = new byte[mqttPublishMessage.payload().readableBytes()];
        // 注意，使用readBytes会导致
        mqttPublishMessage.payload().readBytes(headBytes);
        String data = new String(headBytes);
        logger.debug("message publish topicName: {}",mqttPublishMessage.variableHeader().topicName());
        logger.debug("message publish data--: {}",data);
        logger.debug("message publish all--:{}",mqttPublishMessage.toString());

        switch (qos) {
            case AT_MOST_ONCE: // 最多一次
                break;
            case AT_LEAST_ONCE:// 至少一次
                //构建返回报文，可变报头
                MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack
                        = MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId());
                // 构建返回报文，固定报头
                MqttFixedHeader mqttFixedHeaderBack
                        = new MqttFixedHeader(MqttMessageType.PUBACK,mqttFixedHeaderInfo.isDup(),MqttQoS.AT_MOST_ONCE,mqttFixedHeaderInfo.isRetain(),0x02);
                // 构建PUBACK消息体
                MqttPubAckMessage pubAck = new MqttPubAckMessage(mqttFixedHeaderBack,mqttMessageIdVariableHeaderBack);
                logger.info("message puback back--: "+pubAck.toString());
                channel.writeAndFlush(pubAck);
                break;
            case EXACTLY_ONCE: // 刚好一次
                //构建返回报文，可变报头
                MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack2
                        = MqttMessageIdVariableHeader.from(mqttPublishMessage.variableHeader().packetId());
                // 构建返回报文，固定报头
                MqttFixedHeader mqttFixedHeaderBack2
                        = new MqttFixedHeader(MqttMessageType.PUBREC,false,MqttQoS.AT_MOST_ONCE,false,0x02);
                // 构建PUBACK消息体
                MqttPubAckMessage pubAck2 = new MqttPubAckMessage(mqttFixedHeaderBack2,mqttMessageIdVariableHeaderBack2);
                logger.info("message puback2 back--: "+pubAck2.toString());
                channel.writeAndFlush(pubAck2);
                break;
            default:
                break;
        }
    }

    /**
     * 发布完成 qos=2
     * @param channel
     * @param mqttMessage
     */
    public static void pubcomp(Channel channel, MqttMessage mqttMessage){
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader) mqttMessage.variableHeader();
        // 构建返回报文，固定报头
        MqttFixedHeader mqttFixedHeaderBack = new MqttFixedHeader(MqttMessageType.PUBCOMP,false,MqttQoS.AT_MOST_ONCE,false,0x02);
        // 构建返回报文， 可变报头
        MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack
                = MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId());
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeaderBack,mqttMessageIdVariableHeaderBack);
        logger.info("message pubcomp back--: "+mqttMessageBack.toString());
        channel.writeAndFlush(mqttMessageBack);
    }

    /**
     * 订阅确认
     * @param channel
     * @param mqttMessage
     */
    public static void suback(Channel channel, MqttMessage mqttMessage){
        MqttSubscribeMessage mqttSubscribeMessage = (MqttSubscribeMessage) mqttMessage;
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = mqttSubscribeMessage.variableHeader();
        // 构建返回报文, 可变报头
        MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack
                =MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId());
        Set<String> topics = mqttSubscribeMessage.payload().topicSubscriptions()
                .stream()
                .map(MqttTopicSubscription::topicName)
                .collect(Collectors.toSet());
        List<Integer> grantedQoSLevels = new ArrayList<>(topics.size());
        for (int i = 0; i < topics.size(); i++) {
            grantedQoSLevels.add(mqttSubscribeMessage.payload()
                    .topicSubscriptions().get(i).qualityOfService().value());
        }
        // 构建返回报文 有效负载
        MqttSubAckPayload payloadBack = new MqttSubAckPayload(grantedQoSLevels);
        // 构建返回报文 固定报头
        MqttFixedHeader mqttFixedHeaderBack
                = new MqttFixedHeader(MqttMessageType.SUBACK,false,MqttQoS.AT_MOST_ONCE,false,2+topics.size());
        MqttSubAckMessage subAck = new MqttSubAckMessage(mqttFixedHeaderBack, mqttMessageIdVariableHeaderBack, payloadBack);
        logger.info("message suback back--: "+subAck.toString());

        channel.writeAndFlush(subAck);
    }

    /**
     * 取消订阅确认
     * @param channel
     * @param mqttMessage
     */
    public static void unsuback(Channel channel, MqttMessage mqttMessage){
        MqttMessageIdVariableHeader mqttMessageIdVariableHeader = (MqttMessageIdVariableHeader)mqttMessage.variableHeader();
        // 构建返回报文 可变报头
        MqttMessageIdVariableHeader mqttMessageIdVariableHeaderBack
                = MqttMessageIdVariableHeader.from(mqttMessageIdVariableHeader.messageId());
        // 构建返回报文 固定报头
        MqttFixedHeader mqttFixedHeaderBack
                = new MqttFixedHeader(MqttMessageType.UNSUBACK,false,MqttQoS.AT_MOST_ONCE,false,2);
        // 构建返回报文 取消订阅确认
        MqttUnsubAckMessage unSubAck = new MqttUnsubAckMessage(mqttFixedHeaderBack,mqttMessageIdVariableHeaderBack);
        logger.info("message unsuback back--: "+unSubAck.toString());

        channel.writeAndFlush(unSubAck);
    }

    /**
     * 心跳响应
     * @param channel
     * @param mqttMessage
     */
    public static void pingresp(Channel channel, MqttMessage mqttMessage){
        // 心跳响应报文，固定报文
        MqttFixedHeader mqttFixedHeader
                = new MqttFixedHeader(MqttMessageType.PINGRESP,false,MqttQoS.AT_MOST_ONCE,false,0);
        MqttMessage mqttMessageBack = new MqttMessage(mqttFixedHeader);
        logger.info("message pingresp back--: "+mqttMessageBack.toString());
        channel.writeAndFlush(mqttMessageBack);
    }

    /**
     * 处理qos为1时的PUBACK
     * @param channel
     * @param mqttMessage
     */
    public static void pubackQos1(Channel channel, MqttMessage mqttMessage){
        MqttPubAckMessage mqttPubAckMessage =(MqttPubAckMessage) mqttMessage;
        int messageId =mqttPubAckMessage.variableHeader().messageId();
        logger.info("message pubackQos1 get--: messageId is {}",messageId);
    }

}
