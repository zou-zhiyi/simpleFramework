package com.zzy.core.interfaces;

import java.util.List;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName beanController.java
 * @Description TODO
 * @createTime 2022年04月19日 19:20:00
 */
public interface BeanController {
    void registBean(Object bean);
    void cancelBean(String className);
    List<String> getBeanList();
    Object getBean(String className);
}
