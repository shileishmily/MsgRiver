package com.youguu.river.broker;


import com.youguu.river.common.model.RemoteChannelData;
import com.youguu.river.common.msg.SubscribeMessage;

public interface ConsumerMessageListener {

    void hookConsumerMessage(SubscribeMessage msg, RemoteChannelData channel);
}
