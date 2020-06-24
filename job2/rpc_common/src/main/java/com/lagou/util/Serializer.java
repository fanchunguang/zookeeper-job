package com.lagou.util;

import java.io.IOException;

/**
 * @author fanchg
 * @date: 2020/4/12 10:40 下午
 */
public interface Serializer {

    /**
     * java对象转换为二进制
     *
     * @param object
     * @return
     */
    byte[] serialize(Object object) throws IOException;

    /**
     * 二进制转换成java对象
     *
     * @param clazz
     * @param bytes
     * @param <T>
     * @return
     */

    <T> T deserialize(Class<T> clazz, byte[] bytes) throws IOException;
}
