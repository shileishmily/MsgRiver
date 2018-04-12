package com.youguu.river.broker.spring;

import com.youguu.river.broker.server.BrokerServer;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class AvatarMQServer extends BrokerServer implements ApplicationContextAware, InitializingBean {

    private String serverAddress;

    public AvatarMQServer(String serverAddress) {
        super(serverAddress);
    }

    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        System.out.println("AvatarMQ Server Start Success!");
    }

    public void afterPropertiesSet() throws Exception {
        init();
        start();
    }
}
