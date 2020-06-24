package com.lagou.handler;

import com.lagou.pojo.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.lang.reflect.Method;

public class UserServerHandler1 extends ChannelInboundHandlerAdapter {

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        RpcRequest request= (RpcRequest) msg;
        String className = request.getClassName();
        String methodName = request.getMethodName();
        Class<?>[] parameterTypes = request.getParameterTypes();
        Object[] parameters = request.getParameters();

        //反射调用方法
        /*Class<?> clazz  = Class.forName(className);
        Map<String, ?> beansMap = SpringContextUtil.getBeansOfType(clazz);
        //调用方法
        Method method = clazz.getMethod(methodName, parameterTypes);

        for (Map.Entry<String, ?> stringEntry : beansMap.entrySet()) {
            Object bean = stringEntry.getValue();
            method.invoke(bean,parameters);
        }*/
        Class<?> clazz;
        if(className.equals("com.lagou.service.UserService")){
            clazz=Class.forName("com.lagou.service.UserServiceImpl1");
        }else{
            clazz=Class.forName(className);
        }

        System.out.println("UserServerHandler1.channelRead "+clazz.getSimpleName());
        //创建实例
        Object bean = clazz.newInstance();
        Method method = clazz.getMethod(methodName, parameterTypes);
        method.invoke(bean,parameters);

        //写会数据
        ctx.writeAndFlush("success");

    }


}
