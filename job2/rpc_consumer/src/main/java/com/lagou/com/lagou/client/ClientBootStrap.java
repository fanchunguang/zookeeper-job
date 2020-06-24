package com.lagou.com.lagou.client;

import com.alibaba.fastjson.JSON;
import com.lagou.RpcConsumer;
import com.lagou.com.lagou.listener.LagouZkChildListener;
import com.lagou.service.UserService;
import com.lagou.zkclient.ZkData;
import org.I0Itec.zkclient.ZkClient;

import java.util.*;

public class ClientBootStrap {

    public static String PARENTNODE="/lg-rpc-server";
    public static String provideName = "UserService#sayHello#";
    //存储rpc连接地址和代理对象的map
    public static Map<String,UserService> proxyMap=new HashMap<String, UserService>();
    //存储rpc连接地址和zk节点map
    public static Map<String,String> nodeMap=new HashMap<String, String>();

    public static void main(String[] args) throws InterruptedException {
        //1.创建zkclient
        ZkClient zkClient = new ZkClient("192.168.1.45:2181,192.168.1.45:2182,192.168.1.45:2183");
        System.out.println("会话被创建了..");

        // 获取子节点列表
        List<String> servers = zkClient.getChildren(PARENTNODE);

        for (String server : servers) {
            String json = zkClient.readData(PARENTNODE + "/" + server);
            ZkData zkData = JSON.parseObject(json, ZkData.class);

            UserService userService = startClientByZkNode(zkData);
            String hostname = zkData.getServer();
            int port = zkData.getPort();
            String key = hostname + ":" + port;
            proxyMap.put(key,userService);
            nodeMap.put(key,server);
        }

        //发送请求线程
        ClientStrap clientStrap = new ClientStrap(zkClient);
        Thread thread = new Thread(clientStrap); //开启调用线程
        thread.setName("task-producer");
        thread.start();

        //注册子节点监听事件
        //LagouZkChildListener lagouZkChildListener = new LagouZkChildListener(zkClient,PARENTNODE,clientStrap);
        LagouZkChildListener lagouZkChildListener = new LagouZkChildListener(zkClient,PARENTNODE);
        zkClient.subscribeChildChanges(PARENTNODE,lagouZkChildListener);

        Thread.sleep(Integer.MAX_VALUE);

    }

    /**
     * 获取代理对象
     * @return
     */
    public static ZkData getProxy4Map(ZkClient zkClient){

        //获取所有子节点读取配置
        List<String> childrens = zkClient.getChildren(PARENTNODE);
        List<ZkData> zkDatas = new ArrayList<ZkData>();
        for (String children : childrens) {
            String json = zkClient.readData(PARENTNODE + "/" + children);
            ZkData zkData = JSON.parseObject(json, ZkData.class);
            zkDatas.add(zkData);
        }
        //现在的时间戳
        final long nts = System.currentTimeMillis();
        Collections.sort(zkDatas, new Comparator<ZkData>() {
            public int compare(ZkData o1, ZkData o2) {
                Long o1RT = o1.getRespTime();
                Long o2RT = o2.getRespTime();

                Long o1TS = o1.getTimestamp(); //最后响应时间戳,没填默认为0
                Long o2TS = o2.getTimestamp(); //最后响应时间戳,没填默认为0

                if((nts-o1TS) > 5000){ //没有最后执行时间或者 最后时间在5s之前认为失效，设置为Long.max_value
                    o1RT=Long.MAX_VALUE;
                }
                if((nts-o2TS) > 5000){ //
                    o2RT=Long.MAX_VALUE;
                }

                if(o1RT > o2RT){  //倒叙排,时间大的放在队列后面，最后返回列表队首的对象
                    return -1;
                }else if(o1RT < o2RT){
                    return 1;
                }else { //时间相等
                    return 0;
                }
            }
        });

        if(zkDatas.size()!=0){  //有服务器在线
            return zkDatas.get(0);
        }else {
            return null;
        }

    }

    /**
     * 根据zk节点信息启动netty客户端
     */
    public static UserService startClientByZkNode(ZkData zkData){
        String hostname = zkData.getServer();
        Integer port = zkData.getPort();

        System.out.println("ClientStrap run: "+hostname+":"+port);
        RpcConsumer rpcConsumer = new RpcConsumer();
        UserService proxy = (UserService) rpcConsumer.createProxy(UserService.class,provideName,hostname,port);
        return proxy;
    }
}
