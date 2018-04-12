package com.youguu.river.common.model;

public enum MessageType {

    AvatarMQSubscribe(1),
    AvatarMQUnsubscribe(2),
    AvatarMQMessage(3),
    AvatarMQProducerAck(4),
    AvatarMQConsumerAck(5),
    AvatarMQHeartBeat(6);

    private int messageType;

    private MessageType(int messageType) {
        this.messageType = messageType;
    }

    int getMessageType() {
        return messageType;
    }
}
