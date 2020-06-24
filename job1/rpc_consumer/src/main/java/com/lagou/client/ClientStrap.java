package com.lagou.client;

import com.lagou.RpcConsumer;
import com.lagou.service.UserService;

public class ClientStrap implements Runnable{
    public static String PATH="/rpcserver";
    public static String provideName = "UserService#sayHello#";
    private String hostname;
    private Integer port;

    public ClientStrap(String hostname, Integer port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void run() {
        System.out.println("ClientStrap run: "+hostname+":"+port);
        RpcConsumer consumer = new RpcConsumer();
        UserService service = (UserService) consumer.createProxy(UserService.class, provideName, PATH,"127.0.0.1",8990);
        while (true){
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(service.sayHello(String.format("client request server %s:%d ",hostname,port)));
        }
    }
}
