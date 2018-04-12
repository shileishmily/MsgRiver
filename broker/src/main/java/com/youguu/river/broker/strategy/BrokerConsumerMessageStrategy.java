package com.youguu.river.broker.strategy;
import com.youguu.river.broker.ConsumerMessageListener;
import com.youguu.river.broker.ProducerMessageListener;
import com.youguu.river.common.launcher.SendMessageLauncher;
import com.youguu.river.common.core.CallBackInvoker;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import io.netty.channel.ChannelHandlerContext;

public class BrokerConsumerMessageStrategy implements BrokerStrategy {

    public BrokerConsumerMessageStrategy() {

    }

    public void messageDispatch(RequestMessage request, ResponseMessage response) {
        String key = response.getMsgId();
        if (SendMessageLauncher.getInstance().trace(key)) {
            CallBackInvoker<Object> future = SendMessageLauncher.getInstance().detach(key);
            if (future == null) {
                return;
            } else {
                future.setMessageResult(request);
            }
        } else {
            return;
        }
    }

    public void setHookProducer(ProducerMessageListener hookProducer) {

    }

    public void setHookConsumer(ConsumerMessageListener hookConsumer) {

    }

    public void setChannelHandler(ChannelHandlerContext channelHandler) {

    }
}
