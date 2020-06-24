package com.lagou.server;

import com.lagou.util.RpcDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.stereotype.Component;

/**
 *
 */
@Component
public class NettyServer {

    private static NioEventLoopGroup bossGroup = new NioEventLoopGroup();
    private static NioEventLoopGroup workGroup = new NioEventLoopGroup();
    private Channel channel;
    private static final String SERVERPATH="/rpcserver";

    public static ChannelFuture startServer(String host, int port) throws InterruptedException {
        ServerBootstrap bootstrap = new ServerBootstrap();
        ChannelFuture channelFuture = null;
        final UserServerHandler serverHandler = new UserServerHandler();
        try {
            bootstrap.group(bossGroup, workGroup)
                    //指定服务器监听的通道
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<NioSocketChannel>() {
                        @Override
                        protected void initChannel(NioSocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            //解决粘包/半包,根据消息长度自动拆包
                            pipeline.addLast(new LengthFieldBasedFrameDecoder(65535, 0, 2, 0, 2));
                            //返回给客户端，编码方式
                            pipeline.addLast(new StringEncoder());
                            //服务端解析客户端的入参
                            pipeline.addLast(new RpcDecoder());
                            pipeline.addLast(serverHandler);
                        }
                    });
            channelFuture = bootstrap.bind(host, port).sync();
            //向zookeeper注册服务信息
            registerZookeeper(host, port);
            channelFuture.channel();
            System.out.println(String.format("==================>NettyServer 启动成功 ip %s port %s",host,String.valueOf(port)));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (channelFuture != null && channelFuture.isSuccess()) {
                System.out.println("Netty server listening " + host + " on port " + port + " and ready for connections...");
            } else {
                System.out.println("Netty server start up Error!");
            }
        }
        return channelFuture;
    }

    /**
     * 注册Zookeeper
     *
     * @param ip
     * @param port
     */
    public static void registerZookeeper(String ip, int port) throws Exception {
        //创建会话
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(policy)
                .namespace("base")
                .build();
        client.start();
        //创建
        client.create().creatingParentsIfNeeded().withMode(CreateMode.EPHEMERAL).forPath(SERVERPATH+"/server"+port, (ip + "," + port).getBytes());
    }

    public static void delZookeeper(String path) throws Exception {
        //创建会话
        RetryPolicy policy = new ExponentialBackoffRetry(1000, 3);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183")
                .sessionTimeoutMs(5000)
                .connectionTimeoutMs(3000)
                .retryPolicy(policy)
                .namespace("base")
                .build();
        client.start();
        client.delete().forPath(path);
    }

    public void destory(int port) throws Exception {
        System.out.println("==========>shutdown netty server begin");
        workGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
        System.out.println("==========>shutdown netty server end");
        //删除zookeeper节点
        delZookeeper(SERVERPATH+"/server" + port);
    }
}
