package com.lagou;

import com.lagou.com.lagou.handler.UserClientHanlder;
import com.lagou.pojo.RpcRequest;
import com.lagou.util.JSONSerializer;
import com.lagou.util.RpcDecoder;
import com.lagou.util.RpcEncoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RpcConsumer {
    private static ExecutorService executor =
            Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
    private static UserClientHanlder client;


    public Object createProxy(final Class<?> serviceClass, final String providerName,final String host,final int port) {
        return Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                new Class[]{serviceClass}, new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (client == null) {
                            initClient(host,port);
                        }
                        //设置参数
                        RpcRequest rpcRequest = new RpcRequest();
                        rpcRequest.setRequestId(UUID.randomUUID().toString());
                        rpcRequest.setClassName(serviceClass.getName());
                        rpcRequest.setMethodName(providerName.substring(providerName.indexOf("#") + 1,
                                providerName.lastIndexOf("#")));
                        rpcRequest.setParameters(new Object[]{args[0]});
                        Class<?>[] parameterTypes = new Class<?>[1];
                        parameterTypes[0] = String.class;
                        rpcRequest.setParameterTypes(parameterTypes);
                        client.setPara(rpcRequest);
                        return executor.submit(client).get();
                    }
                });
    }

    public static void initClient(String host,int port) throws InterruptedException {
        client = new UserClientHanlder();
        EventLoopGroup group = new NioEventLoopGroup();
        Bootstrap bootStrap = new Bootstrap();
        bootStrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        ChannelPipeline p = ch.pipeline();
                        ////客户端调用服务端，编码方式
                        p.addLast(new RpcEncoder(RpcRequest.class, new JSONSerializer()));
                        p.addLast(new StringDecoder());
                        p.addLast(client);
                    }
                });

        bootStrap.connect(host, port).sync();

    }
}
