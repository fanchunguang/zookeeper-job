package com.lagou.server;

public class Server1 {
    public static void main(String[] args) throws InterruptedException {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.startServer("127.0.0.1",9998,0);
    }
}
