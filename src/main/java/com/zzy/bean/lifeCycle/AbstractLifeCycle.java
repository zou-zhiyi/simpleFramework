package com.zzy.bean.lifeCycle;

import com.zzy.bean.lifeCycle.interfaces.LifeCycle;
import com.zzy.bean.lifeCycle.interfaces.LifeCycleListener;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author zzy
 * @version 1.0.0
 * @ClassName AbstractLifeCycle.java
 * @Description 抽象的实现了生命周期的类，所有组件都通过这个类进行实现与管理生命周期
 * @createTime 2022年01月14日 14:41:00
 */
public abstract class AbstractLifeCycle implements LifeCycle {

    private List<LifeCycleListener> lifeCycleListeners = new ArrayList<LifeCycleListener>();

    protected volatile LifeCycleState state = LifeCycleState.NULL;

    public AbstractLifeCycle(){

    }

    /**
     * 初始化调用，初始化组件
     * @param map 用于给组件传参使用
     * @throws Exception 抛出的异常
     */
    @Override
    public void init0(Map<String,Object> map) throws Exception{
        if (state != LifeCycleState.NULL && state != LifeCycleState.DESTROYED){
            return;
        }
        setState(LifeCycleState.INITING);
        try {
            init(map);
        } catch (Exception e){
            setState(LifeCycleState.FAILED);
            throw e;
        }
        setState(LifeCycleState.INITED);
        setState(LifeCycleState.RUNNING);
    }

    /**
     * 组件执行方法时调用
     * @param map 用于给组件传参
     * @throws Exception 抛出的异常
     */
    @Override
    public void start0(Map<String,Object> map) throws Exception {
//        考虑到多线程的情况，STARTED和STARTING状态不考虑，一律为RUNNING状态
//        if ((state.compareAge(LifeCycleState.RUNNING)>0)&&(state.compareAge(LifeCycleState.INITED)<0)){
//            return;
//        }
//        setState(LifeCycleState.STARTING);
        try {
            start(map);
        } catch (Exception e){
            setState(LifeCycleState.FAILED);
            throw e;
        }
//        setState(LifeCycleState.STARTED);
//        setState(LifeCycleState.RUNNING);
    }

    /**
     * 组件重启
     * @param map 重启组件时的传参
     * @throws Exception 抛出的异常
     */
    @Override
    public void restart0(Map<String,Object> map) throws Exception{
        if(state.compareAge(LifeCycleState.RUNNING)>0){
            return;
        }
        setState(LifeCycleState.RESTARTING);
        try {
            restart(map);
        } catch (Exception e){
            setState(LifeCycleState.FAILED);
            throw e;
        }
        setState(LifeCycleState.RESTARTED);
        setState(LifeCycleState.RUNNING);
    }

    /**
     * 摧毁组件时执行的方法
     * @param map 摧毁组件时的传参
     * @throws Exception 抛出的异常
     */
    @Override
    public void destroy0(Map<String,Object> map) throws Exception{
        setState(LifeCycleState.DESTROYING);
        try {
            destroy(map);
        } catch (Exception e){
            setState(LifeCycleState.FAILED);
            throw e;
        }
        setState(LifeCycleState.DESTROYED);
    }

    private void setState(LifeCycleState state){
        setState(state,true);
    }

    private void setState(LifeCycleState state,boolean onEvent) {
        this.state=state;
        if (onEvent){
            onLifeCycleEvent(new LifeCycleEvent(this,this,state));
        }
    }

    /**
     * 事件触发，调用监听器
     * @param event 触发的事件
     */
    private void onLifeCycleEvent(LifeCycleEvent event){
        for (LifeCycleListener listener : lifeCycleListeners){
            listener.onLifeCycleEvent(event);
        }
    }

    /**
     * 添加监听器
     * @param lifeCycleListener 被添加的监听器
     */
    public void addLifeCycleListener(LifeCycleListener lifeCycleListener){
        lifeCycleListeners.add(lifeCycleListener);
    }

    protected abstract void init(Map<String,Object> map) throws Exception;
    protected abstract void start(Map<String,Object> map) throws Exception;
    protected abstract void restart(Map<String,Object> map) throws Exception;
    protected abstract void destroy(Map<String,Object> map) throws Exception;
}
