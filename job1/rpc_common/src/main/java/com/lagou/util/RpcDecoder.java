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

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf buf, List<Object> out) throws Exception {
        //读取字节数组
        int length = buf.readableBytes();
        if (length <= 0) {
            return;
        }
        byte[] data = new byte[length];
        buf.readBytes(data, buf.readerIndex(), length);
//        buf.readBytes(data);
        //将字节数组使用反序列化为对象
        JSONSerializer jsonSerializer = new JSONSerializer();
        RpcRequest deserialize = jsonSerializer.deserialize(RpcRequest.class, data);
        out.add(deserialize);
    }

}
