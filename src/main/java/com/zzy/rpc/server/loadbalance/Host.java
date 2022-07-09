package com.zzy.rpc.server.loadbalance;

import io.netty.channel.Channel;
import lombok.Data;

import java.util.List;

/**
 * @author admin
 * @version 1.0.0
 * @ClassName Host.java
 * @Description 负载均衡中的主机
 * @createTime 2022年04月20日 15:47:00
 */
@Data
public class Host {
    private Channel channel;
    private String HostId;
    private long version;
    private List<String> beanList;
}
