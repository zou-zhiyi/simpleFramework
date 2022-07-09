package com.zzy.mqtt.topic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName TopicLevel.java
 * @Description 用于实现mqtt的主题层级匹配,基本实现完成，未实现$匹配
 * @createTime 2022年02月08日 13:51:00
 */
public class TopicLevel {
    // 当前层次主题名
    private String currentLevelName;
    // 下一层的所有主题
    private List<TopicLevel> nextTopicLevelList;
    // 计数器，用于实现取消订阅时进行剪枝
    private AtomicInteger count = new AtomicInteger();

    public String getCurrentLevelName() {
        return currentLevelName;
    }

    public void setCurrentLevelName(String currentLevelName) {
        this.currentLevelName = currentLevelName;
    }

    public List<TopicLevel> getNextTopicLevelList() {
        return nextTopicLevelList;
    }

    public void setNextTopicLevelList(List<TopicLevel> nextTopicLevelList) {
        this.nextTopicLevelList = nextTopicLevelList;
    }

    public TopicLevel(String currentLevelName) {
        this.currentLevelName = currentLevelName;
        nextTopicLevelList = new ArrayList<>();
    }

    private TopicLevel existsInList(String topicName){
        for (TopicLevel topicLevel: nextTopicLevelList){
            if (topicLevel.getCurrentLevelName().equals(topicName)){
                return topicLevel;
            }
        }
        return null;
    }

    private List<TopicLevel> matchInList(String topicName){
        List<TopicLevel> matchResult = new ArrayList<>();
        for (TopicLevel topicLevel: nextTopicLevelList){
            if (topicLevel.getCurrentLevelName().equals(topicName) || topicLevel.getCurrentLevelName().equals("+")
                    ||topicLevel.getCurrentLevelName().equals("#") ){
                matchResult.add(topicLevel);
            }
        }
        return matchResult;
    }

    /**
     * 通过解析发布的主题，得到所有订阅的主题
     * @param publishTopic  切割后的主题层次
     * @param nextPosi      下一层主题的的下标
     * @param maxPosi       最大层数
     * @return              匹配到的所有订阅主题
     */
    public List<String> matching(String[] publishTopic, int nextPosi,int maxPosi){
        if (nextPosi == maxPosi){
            return new ArrayList<String>(){
                {
                    add(currentLevelName);
                }
            };
        }
        String nextTopicName = publishTopic[nextPosi];
        List<String> matchingResult = new ArrayList<>();
        List<TopicLevel> matchInList = matchInList(nextTopicName);
        if (matchInList.isEmpty()) {
            return Collections.EMPTY_LIST;
        }
        else{
            for (TopicLevel topicLevel:matchInList) {
                if (topicLevel.getCurrentLevelName().equals("#")) {
                    matchingResult.add("#");
                }
                else {
                    List<String> matching = topicLevel.matching(publishTopic, nextPosi + 1, maxPosi);
                    matchingResult.addAll(matching);
                }
            }
            for (int i = 0; i < matchingResult.size(); i++) {
                matchingResult.set(i,currentLevelName+"/"+matchingResult.get(i));
            }
        }
        return matchingResult;
    }


    /**
     * 构建主题层次树，通过加入订阅的主题构建主题层次树，用于匹配发布主题
     * @param subTopic  订阅主题层次数组
     * @param nextPosi  下一层主题下标
     * @param maxPosi   最大层数
     */
    public void addSubTopic(String[] subTopic, int nextPosi,int maxPosi){
        // 计数器自增
        count.incrementAndGet();
        if(nextPosi == maxPosi){
            return;
        }
        TopicLevel topicLevel = existsInList(subTopic[nextPosi]);
        if (topicLevel == null){ // 下一层次主题不存在
            topicLevel = new TopicLevel(subTopic[nextPosi]);
            nextTopicLevelList.add(topicLevel);
        }
        topicLevel.addSubTopic(subTopic, nextPosi+1, maxPosi);
    }

    public boolean deleteSubTopic(String[] subTopic, int nextPosi,int maxPosi){
        // 计数器自减
        int i = count.decrementAndGet();
        if(nextPosi == maxPosi){
            return i == 0;
        }
        for (int j = 0; j < nextTopicLevelList.size(); j++) {
            TopicLevel topicLevel = nextTopicLevelList.get(j);
            if (topicLevel.getCurrentLevelName().equals(subTopic[nextPosi])){
                boolean flag = topicLevel.deleteSubTopic(subTopic, nextPosi + 1, maxPosi);
                if (flag){
                    nextTopicLevelList.remove(j);
                }
                break;
            }
        }
        return i == 0;
    }
}
