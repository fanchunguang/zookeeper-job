package com.lagou.com.lagou.listener;

import com.alibaba.fastjson.JSON;
import com.lagou.com.lagou.client.ClientBootStrap;
import com.lagou.service.UserService;
import com.lagou.zkclient.ZkData;
import org.I0Itec.zkclient.IZkChildListener;
import org.I0Itec.zkclient.ZkClient;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

/**
 * 基于ZkClient 子节点监听器
 */
public class LagouZkChildListener implements IZkChildListener {
    private ZkClient zkClient;
    private String parentNode;

    public LagouZkChildListener(ZkClient zkClient, String parentNode) {
        this.zkClient = zkClient;
        this.parentNode = parentNode;
    }

    public void handleChildChange(String parentPath, List<String> currentChilds) throws Exception {
        //clientStrap.wait(); //有变化，先暂停执行
        if(ClientBootStrap.proxyMap.size() < currentChilds.size()){ //有服务器上线
            for (String currentChild : currentChilds) {
                String data = zkClient.readData(parentNode + "/" + currentChild);
                ZkData zkData = JSON.parseObject(data, ZkData.class);
                String server = zkData.getServer();
                int port = zkData.getPort();
                String zkKey = server + ":" + port;
                if(!ClientBootStrap.proxyMap.containsKey(zkKey)){ //上线的服务器
                    UserService userService = ClientBootStrap.startClientByZkNode(zkData);
                    ClientBootStrap.proxyMap.put(zkKey,userService);
                    ClientBootStrap.nodeMap.put(zkKey,currentChild);
                    String msg = String.format("服务器 %s 上线之后的客户端 持有的 服务端对象列表 %s", zkKey, ClientBootStrap.proxyMap.keySet().toString());
                    System.out.println(msg);
                    String nmsg = String.format("服务器 %s 上线之后的客户端 持有的 zk节点列表 %s", zkKey, ClientBootStrap.nodeMap.keySet().toString());
                    System.out.println(nmsg);
                }
            }
        }else{ //服务器下线  只会存在 > 或者小于，没有等于
            HashSet<String> remainKeys = new HashSet<String>();
            for (String currentChild : currentChilds) {
                String data = zkClient.readData(parentNode + "/" + currentChild);
                ZkData zkData = JSON.parseObject(data, ZkData.class);
                String server = zkData.getServer();
                int port = zkData.getPort();
                remainKeys.add(server+":"+port);
            }

            //Map中有的，现在没有则是下线的,使用迭代器删除
            Iterator<String> iter = ClientBootStrap.proxyMap.keySet().iterator();
            while (iter.hasNext()){
                String key = iter.next();
                if(!remainKeys.contains(key)){
                    iter.remove(); //从map中删除
                    String msg = String.format("服务器 %s 下线之后的客户端 持有的 服务端对象列表 %s", key, ClientBootStrap.proxyMap.keySet().toString());
                    System.out.println(msg);
                }
            }
            //删除
            Iterator<String> nodeIter = ClientBootStrap.nodeMap.keySet().iterator();
            while (nodeIter.hasNext()){
                String key = nodeIter.next();
                if(!remainKeys.contains(key)){
                    nodeIter.remove();
                    String msg = String.format("服务器 %s 下线之后的客户端 持有的 zk节点列表 %s", key, ClientBootStrap.nodeMap.keySet().toString());
                    System.out.println(msg);
                }
            }

        }

    }
}
