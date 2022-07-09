package com.zzy.mqtt.topic;

import java.util.*;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName TopicMatchingUtils.java
 * @Description 用于进行mqtt的主题匹配
 * @createTime 2022年02月08日 13:58:00
 */
public class TopicLevelController {
    private List<TopicLevel> basicTopicLevels = new ArrayList<>();

    private String[] getTopicStrings(String topic){
        return topic.split("/");
    }

    /**
     * 通过构造好的匹配树进行主题匹配
     * @param topic
     * @return 返回匹配成功的所有订阅主题
     */
    public Set<String> matchingTopic(String topic){
        String[] publishTopics = getTopicStrings(topic);

        List<String> matchingResult = new ArrayList<>();
        List<TopicLevel> topicLevels = getTopicLevel(publishTopics[0]);
        if (topicLevels == null){
            return Collections.EMPTY_SET;
        }
        for (TopicLevel topicLevel:topicLevels){
            if (topicLevel.getCurrentLevelName().equals("#")) {
                matchingResult.add("#");
            }
            else {
                List<String> matching = topicLevel.matching(publishTopics, 1, publishTopics.length);
                matchingResult.addAll(matching);
            }
        }

        return new HashSet<>(matchingResult);
    }

    private List<TopicLevel> getTopicLevel(String topicName){
        List<TopicLevel> matchResult = new ArrayList<>();
        for (TopicLevel topicLevel:basicTopicLevels){
            if (topicLevel.getCurrentLevelName().equals(topicName) || topicLevel.getCurrentLevelName().equals("+")
                    ||topicLevel.getCurrentLevelName().equals("#") ){
                matchResult.add(topicLevel);
            }
        }
        return matchResult;
    }

    public void addSubTopic(String topicName){
        String[] topicStrings = getTopicStrings(topicName);
        String tempString = topicStrings[0];
        TopicLevel topicLevel = null;
        for (TopicLevel topicLevel0:basicTopicLevels){
            if (topicLevel0.getCurrentLevelName().equals(tempString)){
                topicLevel = topicLevel0;
            }
        }
        if (topicLevel == null){// 说明当前订阅的层次主题不存在
            topicLevel = new TopicLevel(tempString);
            basicTopicLevels.add(topicLevel);
        }
        topicLevel.addSubTopic(topicStrings,1,topicStrings.length);
    }

    /**
     * 将主题从构造树删除
     * @param topicName
     */
    public void deleteTopic(String topicName){
        String[] topicStrings = getTopicStrings(topicName);
        String tempString = topicStrings[0];

        for (int i = 0; i < basicTopicLevels.size(); i++) {
            TopicLevel topicLevel = basicTopicLevels.get(i);
            if (topicLevel.getCurrentLevelName().equals(tempString)){
                boolean flag = topicLevel.deleteSubTopic(topicStrings, 1, topicStrings.length);
                if (flag){
                    basicTopicLevels.remove(i);
                }
                break;
            }
        }
    }

    public void clear(){
        basicTopicLevels.clear();
    }
}
