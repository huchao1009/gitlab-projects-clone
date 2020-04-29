package com.opensource.gitlab.configuration;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "component.rest")
public class RestProperties {
    /**
     * 指从连接池获取连接的timeout
     */
    private int connectionRequestTimeout = 5000;

    /**
     * 指客户端和服务器建立连接的timeout，就是http请求的三个阶段，
     * 一：建立连接；二：数据传送；三，断开连接。
     * 超时后会ConnectionTimeOutException
     */
    private int connectTimeout = 5000;

    /**
     * 指客户端从服务器读取数据的timeout，超出后会抛出SocketTimeOutException
     */
    private int readTimeout = 10000;
}
