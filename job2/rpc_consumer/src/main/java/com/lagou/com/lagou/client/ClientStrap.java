package com.lagou.com.lagou.client;

import com.alibaba.fastjson.JSON;
import com.lagou.service.UserService;
import com.lagou.zkclient.ZkData;
import org.I0Itec.zkclient.ZkClient;

public class ClientStrap implements Runnable {
    private static final String PARENTNODE="/lg-rpc-server";
    private ZkClient curatorFramework;

    public ClientStrap(ZkClient curatorFramework){
        this.curatorFramework=curatorFramework;
    }

    @Override
    public void run() {
        while(true){
            try {
                Thread.sleep(2000);
                if(ClientBootStrap.proxyMap.size()>0){ //有数据
                    //获取执行的代理对象
                    ZkData data = ClientBootStrap.getProxy4Map(curatorFramework);
                    String server = data.getServer();
                    int port = data.getPort();
                    //代理对象map和节点map的key
                    String key = server + ":" + port;
                    UserService proxy = ClientBootStrap.proxyMap.get(key);
                    if(proxy!=null){ //有服务器上线
                        String nodepath = ClientBootStrap.nodeMap.get(key);
                        long start= System.currentTimeMillis();
                        //调用方法
                        String clien_request = proxy.sayHello("clien request "+key);
                        long end= System.currentTimeMillis();
                        long respTime = end-start;
                        //设置响应时间和时间戳
                        data.setRespTime(respTime);
                        data.setTimestamp(end);
                        //json成str
                        String json = JSON.toJSONString(data);
                        //写会zk节点
                        curatorFramework.writeData(PARENTNODE+"/"+nodepath,json);

                        System.out.println(clien_request);
                    }
                }

            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
