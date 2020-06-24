package com.lagou;

import com.lagou.server.NettyServer;
import io.netty.channel.ChannelFuture;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SpringBootNettyApplication implements CommandLineRunner {
    @Autowired
    private NettyServer nettyServer;
    @Autowired
    private NettyServer nettyServer1;

    @Value("${netty.host}")
    private String hostname;
    @Value("${netty.port}")
    private int port;

    public static void main(String[] args) {
        SpringApplication.run(SpringBootNettyApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ChannelFuture future = nettyServer.startServer(hostname, port);
        ChannelFuture future1 = nettyServer1.startServer(hostname, 9090);
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    nettyServer.destory(port);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //服务端管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
        future.channel().closeFuture().syncUninterruptibly();

        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                try {
                    nettyServer1.destory(9090);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        //服务端管道关闭的监听器并同步阻塞,直到channel关闭,线程才会往下执行,结束进程
        future1.channel().closeFuture().syncUninterruptibly();
    }
}
