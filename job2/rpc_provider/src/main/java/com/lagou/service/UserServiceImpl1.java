package com.lagou.service;

//@Service
public class UserServiceImpl1 implements UserService {

    public String sayHello(String word) {
        System.out.println("调用成功--参数 "+word);
        return "调用成功--参数 "+word;
    }



}
