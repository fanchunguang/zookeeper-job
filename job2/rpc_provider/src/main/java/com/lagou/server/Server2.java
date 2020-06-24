package com.lagou.server;

public class Server2 {
    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.startServer("127.0.0.1",9999,1);
    }
}
