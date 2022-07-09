package com.zzy.core.interfaces;

import com.alibaba.fastjson.JSONObject;
import com.zzy.core.bean.ConfigRequest;
import com.zzy.core.bean.ControlRequest;
import com.zzy.core.bean.StatusRequest;

import java.util.List;
import java.util.Map;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName CoreManager.java
 * @Description TODO
 * @createTime 2022年04月17日 10:08:00
 */
public interface CoreManager {
    JSONObject getComponentStatus(Boolean isRoot, String componentStr) throws Exception;
    Boolean controlMsgExecute(String componentStr, String execution) throws Exception;
    Boolean configChange(JSONObject configBody) throws Exception;
}
