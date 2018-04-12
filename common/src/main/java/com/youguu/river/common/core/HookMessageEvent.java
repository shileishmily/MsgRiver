package com.youguu.river.common.core;

public abstract class HookMessageEvent<T> {

    public void disconnect(T message) {
    }

    public T callBackMessage(T message) {
        return message;
    }

}
