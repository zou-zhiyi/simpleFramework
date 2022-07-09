package com.zzy.core.bean;

import lombok.Data;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName CoreRequest.java
 * @Description TODO
 * @createTime 2022年04月13日 15:25:00
 */
@Data
public class ControlRequest {
    // 用于获取对应容器
    private String componentStr;
    private String execution;
}
