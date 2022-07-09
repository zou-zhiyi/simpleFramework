package com.zzy.rpc.server.loadbalance.interfaces;

import com.zzy.rpc.server.loadbalance.Host;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Balance.java
 * @Description 均衡器
 * @createTime 2022年04月20日 16:02:00
 */
public interface Balance {
    void addHost(Host host);
    Host getHost();
    void removeHost(Host host);
}
