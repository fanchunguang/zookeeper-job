package com.lagou.server;

import com.alibaba.fastjson.JSON;
import com.lagou.handler.UserServerHandler;
import com.lagou.handler.UserServerHandler1;
import com.lagou.pojo.RpcRequest;
import com.lagou.service.UserServiceImpl;
import com.lagou.util.JSONSerializer;
import com.lagou.util.RpcDecoder;
import com.lagou.zkclient.ZkData;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.I0Itec.zkclient.ZkClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@SpringBootApplication(scanBasePackages = "com.lagou")
public class ServerBootstrap {

    public void startServer(String server,int port,int num) throws InterruptedException {
        //启动rpc server
        startRpcServer(server,port,num);
        //开始zk
        ZkClient zkClient = new ZkClient("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183");
        System.out.println("服务器会话被创建了..");
        //创建节点
        /*
            cereateParents : 是否要创建父节点，如果值为true,那么就会递归创建节点
         */
        //先创建父节点
        String parentNode="/lg-rpc-server";
        boolean exists = zkClient.exists(parentNode);
        if(!exists){
            zkClient.createPersistent(parentNode,true);
        }

        ZkData zkData = new ZkData(server, port);
        String json = JSON.toJSONString(zkData);
        //创建服务器临时顺序节点
        zkClient.createEphemeralSequential(parentNode+"/server",json);
        System.out.println("服务器zk节点递归创建完成");

        Thread.sleep(Integer.MAX_VALUE);
    }

    //hostName:ip地址  port:端口号
    public void startRpcServer(String hostName,int port,int num) throws InterruptedException {

        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        io.netty.bootstrap.ServerBootstrap serverBootstrap = new io.netty.bootstrap.ServerBootstrap();
        serverBootstrap.group(bossGroup,workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline pipeline = ch.pipeline();
                        pipeline.addLast(new RpcDecoder(RpcRequest.class,new JSONSerializer()));
                        pipeline.addLast(new StringEncoder());
                        if(0==num){
                            pipeline.addLast(new UserServerHandler());
                        }else{
                            pipeline.addLast(new UserServerHandler1());
                        }

                    }
                });
        serverBootstrap.bind(hostName,port).sync();


    }


}
