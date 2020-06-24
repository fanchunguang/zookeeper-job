package com.lagou;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.lagou.zkconfigure.DataListener;
import com.lagou.zkconfigure.DataSourceConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.sql.DataSource;
import java.util.Properties;

@SpringBootApplication
public class ZkconfigureApplication {
    private static final String PATH="/datainfo";
    public static DataSource dataSource;

    public static void main(String[] args) throws Exception {
        SpringApplication.run(ZkconfigureApplication.class, args);
        //1.将数据库信息注册到zookeeper
        registerZookeeper();
        //2.修改数据源
        Thread.sleep(1000 *60);
        setDataSource();
    }

    /**
     * 修改数据源
     */
    private static void setDataSource() throws Exception {
        ExponentialBackoffRetry backoffRetry=new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183")
                .connectionTimeoutMs(5000)
                .retryPolicy(backoffRetry)
                .build();
        client.start();
        byte[] database =client.getData().forPath(PATH);
        if(database!=null){
            DataSourceConfig config = JSON.parseObject(database, DataSourceConfig.class);
            config.setUrl("jdbc:mysql://127.0.0.1:3306/my_test1?serverTimezone=UTC");
            getDuridDataSource(config);
            //client.setData().forPath(PATH,config.toString().getBytes());
        }

    }

    /**
     * 数据库信息注册到zookeeper并开启监听
     */
    public static void registerZookeeper() throws Exception {
        ExponentialBackoffRetry backoffRetry=new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .connectString("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183")
                .connectionTimeoutMs(5000)
                .retryPolicy(backoffRetry)
                .build();
        client.start();
        if(client.checkExists().forPath(PATH)==null){
            DataSourceConfig dataSourceConfig=new DataSourceConfig();
            dataSourceConfig.setUrl("jdbc:mysql://127.0.0.1:3306/my_test?serverTimezone=UTC");
            dataSourceConfig.setUsername("root");
            dataSourceConfig.setPassword("123456");
            getDuridDataSource(null);
            client.create().withMode(CreateMode.PERSISTENT).forPath(PATH,JSON.toJSONString(dataSourceConfig).getBytes());
        }
        //创建监听
        NodeCache cache=new NodeCache(client,PATH);
        DataListener listener=new DataListener(cache);
        cache.getListenable().addListener(listener);

        byte[] database=client.getData().forPath(PATH);
        if(database.length==0){
            //封装数据库连接池对象 并加入到zookeeper
            DataSourceConfig dataSourceConfig=new DataSourceConfig();
            dataSourceConfig.setUrl("jdbc:mysql://127.0.0.1:3306/my_test?serverTimezone=UTC");
            dataSourceConfig.setUsername("root");
            dataSourceConfig.setPassword("123456");
            getDuridDataSource(null);
            client.setData().forPath(PATH,JSON.toJSONString(dataSourceConfig).getBytes());
        }else {
            byte[] bytes = client.getData().forPath(PATH);
            DataSourceConfig config=JSON.parseObject(bytes,DataSourceConfig.class);
            getDuridDataSource(config);
            System.out.println("do nothing");
        }

    }

    private static void getDuridDataSource(DataSourceConfig dataSourceConfig) throws Exception {
        Properties properties=new Properties();
        if(dataSourceConfig==null){
            properties.setProperty("url","jdbc:mysql://127.0.0.1:3306/my_test?serverTimezone=UTC");
            properties.setProperty("username","root");
            properties.setProperty("password","123456");
        }else{
            properties.setProperty("url",dataSourceConfig.getUrl());
            properties.setProperty("username",dataSourceConfig.getUsername());
            properties.setProperty("password",dataSourceConfig.getPassword());
        }
        dataSource= DruidDataSourceFactory.createDataSource(properties);
    }
}
