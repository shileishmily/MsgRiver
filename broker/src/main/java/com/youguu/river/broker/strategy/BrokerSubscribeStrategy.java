package com.youguu.river.broker.strategy;
import com.youguu.river.broker.ConsumerMessageListener;
import com.youguu.river.broker.ProducerMessageListener;
import com.youguu.river.common.model.MessageType;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.model.RemoteChannelData;
import com.youguu.river.common.msg.SubscribeMessage;
import io.netty.channel.ChannelHandlerContext;

public class BrokerSubscribeStrategy implements BrokerStrategy {

    private ConsumerMessageListener hookConsumer;
    private ChannelHandlerContext channelHandler;

    public BrokerSubscribeStrategy() {

    }

    public void messageDispatch(RequestMessage request, ResponseMessage response) {
        SubscribeMessage subcript = (SubscribeMessage) request.getMsgParams();
        String clientKey = subcript.getConsumerId();
        RemoteChannelData channel = new RemoteChannelData(channelHandler.channel(), clientKey);
        hookConsumer.hookConsumerMessage(subcript, channel);
        response.setMsgType(MessageType.AvatarMQConsumerAck);
        channelHandler.writeAndFlush(response);
    }

    public void setHookConsumer(ConsumerMessageListener hookConsumer) {
        this.hookConsumer = hookConsumer;
    }

    public void setChannelHandler(ChannelHandlerContext channelHandler) {
        this.channelHandler = channelHandler;
    }

    public void setHookProducer(ProducerMessageListener hookProducer) {

    }
}
