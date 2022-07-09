package com.zzy.bean.lifeCycle.interfaces;

import java.util.Map;

/**
 * @author zzy
 * @version 1.0.0
 * @ClassName LifeCycle.java
 * @Description 生命周期接口
 * @createTime 2022年01月14日 14:41:00
 */
public interface LifeCycle {
    void init0(Map<String,Object> map) throws Exception;
    void restart0(Map<String,Object> map) throws Exception;
    void start0(Map<String,Object> map) throws Exception;
    void destroy0(Map<String,Object> map) throws Exception;
}
