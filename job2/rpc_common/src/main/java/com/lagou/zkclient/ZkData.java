package com.lagou.zkclient;

/**
 * zk协议信息
 */
public class ZkData {
    //服务器
    private String server;
    //端口
    private int port;
    //响应时间
    private long respTime;
    //上次执行完毕时间戳
    private long timestamp;

    public ZkData(String server, int port) {
        this.server = server;
        this.port = port;
    }

    public ZkData(String server, int port, long respTime, long timestamp) {
        this.server = server;
        this.port = port;
        this.respTime = respTime;
        this.timestamp = timestamp;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public long getRespTime() {
        return respTime;
    }

    public void setRespTime(long respTime) {
        this.respTime = respTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "ZkData{" +
                "server='" + server + '\'' +
                ", port=" + port +
                ", respTime=" + respTime +
                ", timestamp=" + timestamp +
                '}';
    }
}
