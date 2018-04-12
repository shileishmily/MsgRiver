package com.youguu.river.broker.strategy;
import com.youguu.river.broker.ConsumerMessageListener;
import com.youguu.river.broker.ProducerMessageListener;
import com.youguu.river.common.context.ConsumerContext;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.msg.UnSubscribeMessage;
import io.netty.channel.ChannelHandlerContext;

public class BrokerUnsubscribeStrategy implements BrokerStrategy {

    public BrokerUnsubscribeStrategy() {

    }

    public void messageDispatch(RequestMessage request, ResponseMessage response) {
        UnSubscribeMessage msgUnSubscribe = (UnSubscribeMessage) request.getMsgParams();
        ConsumerContext.unLoad(msgUnSubscribe.getConsumerId());
    }

    public void setHookProducer(ProducerMessageListener hookProducer) {

    }

    public void setHookConsumer(ConsumerMessageListener hookConsumer) {

    }

    public void setChannelHandler(ChannelHandlerContext channelHandler) {

    }
}
