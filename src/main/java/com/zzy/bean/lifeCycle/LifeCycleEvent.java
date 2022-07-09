package com.zzy.bean.lifeCycle;

import java.util.EventObject;

/**
 * @author zzy
 * @version 1.0.0
 * @ClassName LifeCycleEvent.java
 * @Description 生命周期监听事件类
 * @createTime 2022年01月14日 14:41:00
 */
public class LifeCycleEvent extends EventObject {

    private static final long serialVersionUID = 5309094505374429149L;

    private AbstractLifeCycle lifeCycle;

    private LifeCycleState state;

    public LifeCycleEvent(Object source, AbstractLifeCycle lifeCycle, LifeCycleState state) {
        super(source);
        this.lifeCycle=lifeCycle;
        this.state=state;
    }

    public LifeCycleState getState() {
        return state;
    }

    public void setState(LifeCycleState state) {
        this.state = state;
    }

    public AbstractLifeCycle getLifeCycle() {
        return lifeCycle;
    }

    public void setLifeCycle(AbstractLifeCycle lifeCycle) {
        this.lifeCycle = lifeCycle;
    }
}
