package com.youguu.river.broker.strategy;

import com.youguu.river.broker.ConsumerMessageListener;
import com.youguu.river.broker.ProducerMessageListener;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;

public interface BrokerStrategy {

    void messageDispatch(RequestMessage request, ResponseMessage response);

    void setHookProducer(ProducerMessageListener hookProducer);

    void setHookConsumer(ConsumerMessageListener hookConsumer);

    void setChannelHandler(ChannelHandlerContext channelHandler);
}
