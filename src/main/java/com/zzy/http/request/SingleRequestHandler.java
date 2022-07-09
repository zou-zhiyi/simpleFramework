package com.zzy.http.request;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName SingleRequestHandler.java
 * @Description TODO
 * @createTime 2022年03月10日 15:22:00
 */
public abstract class SingleRequestHandler extends BasicRequestHandler {

    private String url;

    private String method;

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getMethodAndUrl() {
        return method+":"+url;
    }
}
