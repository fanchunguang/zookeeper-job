package com.lagou.client;

import com.lagou.RpcConsumer;
import com.lagou.service.UserService;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientBootStrap {
    public static String PATH="/rpcserver";
    public static String provideName = "UserService#sayHello#";

    public static void main(String[] args) throws Exception {
        //1.启动zookeeper客户端
        CuratorFramework zkclient =startZookeeper();
        /*//2.获取服务信息
        List<String> hosts = getHostFromZookeeper(PATH);

        ExecutorService service= Executors.newCachedThreadPool();
        System.out.println("hosts = " + hosts);
        if (hosts!=null && hosts.size()>0) {
            for(String host:hosts){
                startClientByZkNode(zkclient,service,host);
            }
        } else {
            System.out.println("zookeeper当前地址不存在 " + PATH);
        }*/
        //创建监听
        listenZode(zkclient, PATH);
        Thread.sleep(Integer.MAX_VALUE);
    }

    /**
     * 根据zk节点信息启动netty客户端
     */
    public static void startClientByZkNode(CuratorFramework zkClient,ExecutorService executor,String server) throws Exception {
        byte[] bytes = zkClient.getData().forPath(PATH + "/" + server);
        if(bytes!=null){
            String[] ips = new String(bytes).split(",");
            String hostname = ips[0];
            Integer port = Integer.valueOf(ips[1]);

            ClientStrap clientStrap = new ClientStrap(hostname, port);
            executor.submit(clientStrap);

        }

    }

    /**
     *
     * @return
     * @throws Exception
     */
    private static CuratorFramework startZookeeper() throws Exception {
        ExponentialBackoffRetry backoffRetry=new ExponentialBackoffRetry(1000,3);
        CuratorFramework client = CuratorFrameworkFactory
                .builder()
                .retryPolicy(backoffRetry)
                .connectString("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183")
                .namespace("base")
                .build();
        client.start();
        return client;
    }

    /**
     * 从zookeeper中获取IP,端口
     *
     * @param path
     * @return
     */
    public static List<String> getHostFromZookeeper(String path) throws Exception {
        ExponentialBackoffRetry backoffRetry=new ExponentialBackoffRetry(1000,3);
        CuratorFramework client=CuratorFrameworkFactory
                .builder()
                .namespace("base")
                .connectString("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183")
                .retryPolicy(backoffRetry)
                .build();
        client.start();
        List<String> strings = client.getChildren().forPath(path);
        if (strings == null||strings.size()==0)
            return null;
        return strings;
    }

    /**
     * 监听节点
     * @param client
     * @param path
     * @throws Exception
     */
    public static void listenZode(CuratorFramework client, String path) throws Exception {
        if (path == null || "".equals(path)) {
            return;
        }

        PathChildrenCache cache = new PathChildrenCache(client, PATH,false);
        PathChildrenCacheListener listener= new PathChildrenCacheListener() {
            @Override
            public void childEvent(CuratorFramework curatorFramework, PathChildrenCacheEvent event) throws Exception {
                if(PathChildrenCacheEvent.Type.CHILD_ADDED.equals(event.getType())){
                    List<String> String = curatorFramework.getChildren().forPath(path);
                    System.out.println("添加子节点成功 : " + curatorFramework.getData().forPath(path));
                    if(String!=null &&String.size()>0){
                        for(String str:String){
                            ExecutorService service= Executors.newFixedThreadPool(10);
                            byte[] bytes = curatorFramework.getData().forPath(PATH + "/" + str);
                            String[] ips = new String(bytes).split(",");
                            String hostname = ips[0];
                            Integer port = Integer.valueOf(ips[1]);

                            ClientStrap clientStrap = new ClientStrap(hostname, port);
                            service.execute(clientStrap);
                        }

                    }


                }else if(PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(event.getType())){
                    System.out.println("更新子节点成功 : " + curatorFramework.getData().forPath(path));
                }else if(PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(event.getType())){
                    byte[] bytes = curatorFramework.getData().forPath(path);
                    System.out.println("删除子节点成功 : " + curatorFramework.getData().forPath(path));
                    if(bytes!=null){

                    }
                }
            }
        };
        cache.getListenable().addListener(listener);
        cache.start();
    }
}
