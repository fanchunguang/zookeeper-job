package com.lagou.util;

import com.alibaba.fastjson.JSON;

/**
 * @author fanchg
 * @date: 2020/4/12 10:42 下午
 */
public class JSONSerializer implements Serializer {

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }

}
