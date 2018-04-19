package com.youguu.river.broker;
import com.youguu.river.broker.strategy.BrokerStrategyContext;
import com.youguu.river.common.model.MessageType;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.model.MessageSource;
import com.youguu.river.common.netty.ShareMessageEventWrapper;
import io.netty.channel.ChannelHandlerContext;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Broker消息处理hander入口
 */
public class MessageBrokerHandler extends ShareMessageEventWrapper<Object> {

    private AtomicReference<ProducerMessageListener> hookProducer;
    private AtomicReference<ConsumerMessageListener> hookConsumer;
    private AtomicReference<RequestMessage> message = new AtomicReference<>();

    public MessageBrokerHandler() {
        super.setWrapper(this);
    }

    public MessageBrokerHandler buildProducerHook(ProducerMessageListener hookProducer) {
        this.hookProducer = new AtomicReference<>(hookProducer);
        return this;
    }

    public MessageBrokerHandler buildConsumerHook(ConsumerMessageListener hookConsumer) {
        this.hookConsumer = new AtomicReference<>(hookConsumer);
        return this;
    }

    public void handleMessage(ChannelHandlerContext ctx, Object msg) {

        RequestMessage request = message.get();

        //如果是心跳消息，响应返回
        if(MessageType.AvatarMQHeartBeat == request.getMsgType()){
            sendPongMsg(ctx);
            return;
        }

        //业务消息处理
        ResponseMessage response = new ResponseMessage();
        response.setMsgId(request.getMsgId());
        response.setMsgSource(MessageSource.AvatarMQBroker);

        BrokerStrategyContext strategy = new BrokerStrategyContext(request, response, ctx);
        strategy.setHookConsumer(hookConsumer.get());
        strategy.setHookProducer(hookProducer.get());
        strategy.invoke();
    }

    @Override
    public void beforeMessage(Object msg) {
        message.set((RequestMessage) msg);
    }

    /**
     * 服务端读超时，关闭client
     * @param ctx
     */
    @Override
    protected void handleReaderIdle(ChannelHandlerContext ctx) {
        super.handleReaderIdle(ctx);
        System.err.println("client " + ctx.channel().remoteAddress().toString() + " read timeout, disconnect it");
        ctx.close();
    }
}
