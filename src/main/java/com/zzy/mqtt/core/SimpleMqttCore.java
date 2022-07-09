package com.zzy.mqtt.core;

import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttMessage;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName SimpleMessageQueue.java
 * @Description 简易的消息队列，before或after这些可不用
 * @createTime 2022年02月23日 08:38:00
 */
public abstract class SimpleMqttCore extends AbstractMqttCore {
    @Override
    protected void beforeConnack(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterConnack(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforePuback(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterPuback(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforePubcomp(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterPubcomp(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforeSuback(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterSuback(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforeUnsuback(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterUnsuback(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforePingresp(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterPingresp(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforeDisconnect(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterDisconnect(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void beforePubackQos1(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

    @Override
    protected void afterPubackQos1(Channel channel, MqttMessage mqttMessage) throws Exception {

    }

}
