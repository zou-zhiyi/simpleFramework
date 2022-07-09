package com.zzy.rpc.test;

import com.zzy.bean.exception.MyException;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName HelloServiceImpl.java
 * @Description TODO
 * @createTime 2022年04月16日 23:04:00
 */
public class HelloServiceImpl implements HelloService {

    @Override
    public String hello(String name) throws Exception {
        System.out.println(("hello, " + name));
        return "hello, " + name;
    }
}
