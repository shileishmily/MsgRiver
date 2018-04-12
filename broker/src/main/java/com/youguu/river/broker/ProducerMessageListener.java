package com.youguu.river.broker;
import com.youguu.river.common.msg.Message;
import io.netty.channel.Channel;

public interface ProducerMessageListener {

    void hookProducerMessage(Message msg, String requestId, Channel channel);
}
