package com.zzy.http.request;

import java.util.ArrayList;
import java.util.List;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName MultiRequestHandler.java
 * @Description TODO
 * @createTime 2022年03月10日 15:07:00
 */
public abstract class MultiRequestHandler extends BasicRequestHandler {

    private List<String> methodAndUrlList = new ArrayList<>();

    public void addMethodAndUrl(String method, String url) {
        methodAndUrlList.add(method+":"+url);
    }

    public void addMethodAndUrl(String methodAndUrl) {
        methodAndUrlList.add(methodAndUrl);
    }

    public List<String> getMethodAndUrlList() {
        return methodAndUrlList;
    }

    public void setMethodAndUrlList(List<String> methodAndUrlList) {
        this.methodAndUrlList = methodAndUrlList;
    }
}
