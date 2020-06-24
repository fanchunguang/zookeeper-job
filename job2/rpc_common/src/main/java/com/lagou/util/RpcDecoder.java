package com.lagou.util;

import com.lagou.pojo.RpcRequest;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author fanchg
 */
public class RpcDecoder extends ByteToMessageDecoder {

    private Class<?> clazz;
    private Serializer serializer;

    public RpcDecoder(Class<?> clazz, Serializer serializer) {
        this.clazz = clazz;
        this.serializer = serializer;
    }

    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        //System.out.println("provider side:" + in.toString());
        if(clazz != null){
            if(in.readableBytes() < 4){ //首先读取 int类型的长度 byteBuf.writeInt(bytes.length);
                return;
            }
            in.markReaderIndex();
            //读取消息长度
            int msgLength = in.readInt();
            //做readindex
            int length = in.readableBytes();
            if(length < msgLength){
                in.resetReaderIndex();
                return;
            }
            /*while(length < msgLength){
                //等到有足够长度
                length = in.readableBytes();
            }*/
            byte[] bytes = new byte[msgLength];
            in.readBytes(bytes);

            //反序列化对象
            Object object = serializer.deserialize(clazz, bytes);
            out.add(object);

        }

    }

}
