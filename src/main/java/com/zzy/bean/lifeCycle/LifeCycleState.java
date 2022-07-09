package com.zzy.bean.lifeCycle;

/**
 * @author zzy
 * @version 1.0.0
 * @ClassName LifeCycleState.java
 * @Description 生命周期状态枚举类
 * @createTime 2022年01月14日 14:41:00
 */
public enum  LifeCycleState {
    NULL(0),
    INITING(1),
    INITED(2),
    STARTING(3),
    STARTED(4),
    RUNNING(5),
    RESTARTING(6),
    RESTARTED(7),
    DESTROYING(8),
    DESTROYED(9),
    FAILED(10);

    private int age;

    private LifeCycleState(int age){
        this.age=age;
    }

    public int getAge(){
        return age;
    }

    public int compareAge(LifeCycleState lifeCycleState){
        if (age<lifeCycleState.getAge()){
            return -1;
        }
        else if (age == lifeCycleState.getAge()){
            return 0;
        }
        else{
            return 1;
        }
    }
}
