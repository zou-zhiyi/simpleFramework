package com.zzy.mqtt.core;

import com.zzy.bean.component.AbstractServiceComponent;
import io.netty.channel.Channel;
import io.netty.handler.codec.mqtt.MqttFixedHeader;
import io.netty.handler.codec.mqtt.MqttMessage;

import java.util.Map;

import static com.zzy.mqtt.utils.MqttConstances.MQTT_CHANNEL;
import static com.zzy.mqtt.utils.MqttConstances.MQTT_MSG;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MessageQueue.java
 * @Description 用生命周期来实现Mqtt中的消息队列
 * @createTime 2022年02月02日 20:39:00
 */
public abstract class AbstractMqttCore extends AbstractServiceComponent {


    @Override
    protected void start(Map<String, Object> map) throws Exception {
        Channel channel = (Channel) map.get(MQTT_CHANNEL);
        MqttMessage mqttMessage = (MqttMessage) map.get(MQTT_MSG);
        MqttFixedHeader mqttFixedHeader = mqttMessage.fixedHeader();
        switch (mqttFixedHeader.messageType()){
            case CONNECT:
                beforeConnack(channel, mqttMessage);
                connack(channel, mqttMessage);
                afterConnack(channel, mqttMessage);
                break;
            case PUBLISH:
                beforePuback(channel, mqttMessage);
                puback(channel, mqttMessage);
                afterPuback(channel, mqttMessage);
                break;
            case PUBACK:
                beforePubackQos1(channel, mqttMessage);
                pubackQos1(channel, mqttMessage);
                afterPubackQos1(channel, mqttMessage);
                break;
            case PUBREL:
                beforePubcomp(channel, mqttMessage);
                pubcomp(channel, mqttMessage);
                afterPubcomp(channel, mqttMessage);
                break;
            case SUBSCRIBE:
                beforeSuback(channel, mqttMessage);
                suback(channel,mqttMessage);
                afterSuback(channel, mqttMessage);
                break;
            case UNSUBSCRIBE:
                beforeUnsuback(channel, mqttMessage);
                unsuback(channel,mqttMessage);
                afterUnsuback(channel, mqttMessage);
                break;
            case PINGREQ:
                beforePingresp(channel, mqttMessage);
                pingresp(channel,mqttMessage);
                afterPingresp(channel, mqttMessage);
                break;
            case DISCONNECT:
                beforeDisconnect(channel, mqttMessage);
                disconnect(channel, mqttMessage);
                afterDisconnect(channel, mqttMessage);
                break;
            default:
                break;
        }
    }

    protected abstract void beforeConnack(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void connack(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterConnack(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforePuback(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void puback(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterPuback(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforePubcomp(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void pubcomp(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterPubcomp(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforeSuback(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void suback(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterSuback(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforeUnsuback(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void unsuback(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterUnsuback(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforePingresp(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void pingresp(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterPingresp(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforeDisconnect(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void disconnect(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterDisconnect(Channel channel, MqttMessage mqttMessage) throws Exception;

    protected abstract void beforePubackQos1(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void pubackQos1(Channel channel, MqttMessage mqttMessage) throws Exception;
    protected abstract void afterPubackQos1(Channel channel, MqttMessage mqttMessage) throws Exception;


}
