package com.zzy.bean.lifeCycle.interfaces;

import com.zzy.bean.lifeCycle.LifeCycleEvent;

import java.util.EventListener;

/**
 * @author zzy
 * @version 1.0.0
 * @ClassName LifeCycleListener.java
 * @Description 生命周期监听接口
 * @createTime 2022年01月14日 14:41:00
 */
public interface LifeCycleListener extends EventListener {
    void onLifeCycleEvent(LifeCycleEvent event);
}
