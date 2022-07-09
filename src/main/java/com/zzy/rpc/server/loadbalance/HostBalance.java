package com.zzy.rpc.server.loadbalance;

import com.zzy.rpc.server.loadbalance.interfaces.Balance;
import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName HostBalance.java
 * @Description 需要保证线程安全
 * @createTime 2022年04月20日 16:01:00
 */
@Data
public class HostBalance implements Balance {
    // 存储索引的key, 通过查询其中的key，获取对应host
    private List<String> hostKeyList;
    private Map<String, Host> index;
    private volatile AtomicInteger current = new AtomicInteger(0);

    public HostBalance() {
        hostKeyList = new CopyOnWriteArrayList<>();
        index = new ConcurrentHashMap<>();
    }

    @Override
    public synchronized void addHost(Host host) {
        hostKeyList.add(host.getHostId());
        index.put(host.getHostId(), host);
    }

    @Override
    public synchronized Host getHost() {
        if (hostKeyList.size() == 0){
            return null;
        }
        while (current.intValue() >= hostKeyList.size()){
            current.set(0);
        }
        String key = hostKeyList.get(current.intValue());
        current.incrementAndGet();
        Host host = index.get(key);
        return host;
    }

    @Override
    public synchronized void removeHost(Host host) {
        hostKeyList.remove(host);
        index.remove(host.getHostId());
    }
}
