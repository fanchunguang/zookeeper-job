package com.lagou.server;

import com.lagou.pojo.RpcRequest;
import com.lagou.util.AppContextUtil;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
@ChannelHandler.Sharable
public class UserServerHandler extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest rpcRequest = (RpcRequest) msg;

        String methodName = rpcRequest.getMethodName();
        String className = rpcRequest.getClassName();
        //通过类名去获取具体的实现类
        String onlyClassName = className.substring(className.lastIndexOf(".") + 1);
        //去上下文中去获取到，客户端调用的方法的具体实现类
        Object object = AppContextUtil.getObject(onlyClassName);
        //执行客户端想要调用的方法
        Object proxyResult = getJdkProxy(object, methodName, rpcRequest.getParameters());
        ctx.writeAndFlush(proxyResult.toString());
        System.out.println("ctx = " + ctx + ", msg = " + msg);
    }

    /**
     * 利用jdk动态代理最终去执行客户端要调用的方法
     *
     * @param obj
     * @param methodName
     * @param prams
     * @return
     */
    public Object getJdkProxy(Object obj, String methodName, Object[] prams) {
        // 获取代理对象
        return Proxy.newProxyInstance(obj.getClass().getClassLoader(), obj.getClass().getInterfaces(),
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        Object result = null;
                        Method[] methods = obj.getClass().getMethods();
                        for (Method method1 : methods) {
                            //查找要执行的方法
                            if (methodName.equals(method1.getName())) {
                                //通过代理对象执行最终调用的方法
                                result = method1.invoke(obj, prams);
                            }
                        }
                        return result;
                    }
                });
    }
}
