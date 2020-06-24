package com.lagou.zkconfigure;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSON;
import com.lagou.ZkconfigureApplication;
import com.lagou.util.JdbcUtil;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;

import java.util.Properties;

/**
 * 监听节点变化
 */
public class DataListener implements NodeCacheListener {

    private NodeCache nodeCache;
    public DataListener(NodeCache cache) {
        this.nodeCache=cache;
    }

    @Override
    public void nodeChanged() throws Exception {
        ChildData currentData = nodeCache.getCurrentData();
        System.out.println("data :" + currentData);
        JdbcUtil.closeConnection(ZkconfigureApplication.dataSource.getConnection());
        //创建新的连接
        
    }

    private static void getDuridDataSource(String dataInfo) throws Exception {
        Properties properties=new Properties();
        if("".equals(dataInfo)){
            properties.setProperty("url","jdbc:mysql://127.0.0.1:3306/my_test?serverTimezone=UTC");
            properties.setProperty("username","root");
            properties.setProperty("password","123456");
        }else{
            DataSourceConfig obj= JSON.parseObject("",DataSourceConfig.class);
            properties.setProperty("url",obj.getUrl());
            properties.setProperty("username",obj.getUsername());
            properties.setProperty("password",obj.getPassword());
        }
        ZkconfigureApplication.dataSource= DruidDataSourceFactory.createDataSource(properties);
    }
}
