package com.zzy.core.bean;

import lombok.Data;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName StatusRequest.java
 * @Description TODO
 * @createTime 2022年04月14日 15:43:00
 */
@Data
public class StatusRequest {
    private Boolean isRoot;
    private String componentStr;
}
