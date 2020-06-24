package com.lagou.handler;

import com.lagou.pojo.RpcRequest;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.Callable;
@ChannelHandler.Sharable
public class UserClientHanlder extends ChannelInboundHandlerAdapter implements Callable {

    private ChannelHandlerContext context;
    private String result;
    private RpcRequest para;

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        context = ctx;
    }

    /**
     * 收到服务端数据，唤醒等待线程
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        result = msg.toString();
        synchronized (this) {
            notify();
        }

    }

    /**
     * 写出数据，开始等待唤醒
     *
     * @return
     * @throws Exception
     */
    public Object call() throws InterruptedException {
        if (context != null)
            context.writeAndFlush(para);
        synchronized (this) {
            wait();
        }
        return result;
    }

    public void setPara(RpcRequest para) {
        this.para = para;
    }

}
