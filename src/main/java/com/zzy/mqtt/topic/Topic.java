package com.zzy.mqtt.topic;

import io.netty.handler.codec.mqtt.MqttQoS;

import java.util.Objects;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Topic.java
 * @Description TODO
 * @createTime 2022年02月08日 09:51:00
 */
public class Topic {
    private String topicName;
    private MqttQoS qoS;

    public Topic(String topicName, MqttQoS qoS) {
        this.topicName = topicName;
        this.qoS = qoS;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        };
        if (o == null || getClass() != o.getClass()) {
            return false;
        };
        Topic topic = (Topic) o;
        return Objects.equals(topicName, topic.topicName) &&
                qoS.value() == topic.qoS.value();
    }

    @Override
    public int hashCode() {
        return Objects.hash(topicName, qoS);
    }

    public String getTopicName() {
        return topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }

    public MqttQoS getQoS() {
        return qoS;
    }

    public void setQoS(MqttQoS qoS) {
        this.qoS = qoS;
    }
}
