package com.youguu.river.broker.spring;

public interface Container {

    void start();

    void stop();

    Context<?> getContext();
}
