package com.zzy.bean.exception;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MyException.java
 * @Description 用于扩展，暂定继承Exception
 * @createTime 2022年02月11日 16:05:00
 */
public class MyException extends Exception{
    public MyException(String message) {
        super(message);
    }
}
