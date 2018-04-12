package com.youguu.river.broker.strategy;
import com.youguu.river.common.msg.Message;
import com.youguu.river.broker.ConsumerMessageListener;
import com.youguu.river.broker.ProducerMessageListener;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;

public class BrokerProducerMessageStrategy implements BrokerStrategy {

    private ProducerMessageListener hookProducer;
    private ChannelHandlerContext channelHandler;

    public BrokerProducerMessageStrategy() {

    }

    public void messageDispatch(RequestMessage request, ResponseMessage response) {
        Message message = (Message) request.getMsgParams();
        hookProducer.hookProducerMessage(message, request.getMsgId(), channelHandler.channel());
    }

    public void setHookProducer(ProducerMessageListener hookProducer) {
        this.hookProducer = hookProducer;
    }

    public void setChannelHandler(ChannelHandlerContext channelHandler) {
        this.channelHandler = channelHandler;
    }

    public void setHookConsumer(ConsumerMessageListener hookConsumer) {

    }
}
