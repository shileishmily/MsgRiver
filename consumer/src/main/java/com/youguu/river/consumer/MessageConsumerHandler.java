package com.youguu.river.consumer;

import com.youguu.river.common.core.HookMessageEvent;
import com.youguu.river.common.model.MessageSource;
import com.youguu.river.common.model.MessageType;
import com.youguu.river.common.model.RequestMessage;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.msg.ConsumerAckMessage;
import com.youguu.river.common.netty.MessageEventWrapper;
import com.youguu.river.common.netty.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;

public class MessageConsumerHandler extends MessageEventWrapper<Object> {

    private String key;

    public MessageConsumerHandler(MessageProcessor processor) {
        this(processor, null);
        super.setWrapper(this);
    }

    public MessageConsumerHandler(MessageProcessor processor, HookMessageEvent hook) {
        super(processor, hook);
        super.setWrapper(this);
    }

    public void beforeMessage(Object msg) {
        key = ((ResponseMessage) msg).getMsgId();
    }

    public void handleMessage(ChannelHandlerContext ctx, Object msg) {
        if (!factory.traceInvoker(key) && hook != null) {

            ResponseMessage message = (ResponseMessage) msg;
            ConsumerAckMessage result = (ConsumerAckMessage) hook.callBackMessage(message);
            if (result != null) {
                RequestMessage request = new RequestMessage();
                request.setMsgId(message.getMsgId());
                request.setMsgSource(MessageSource.AvatarMQConsumer);
                request.setMsgType(MessageType.AvatarMQMessage);
                request.setMsgParams(result);

                ctx.writeAndFlush(request);
            }
        }
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);
        sendPingMsg(ctx);//给Server发送心跳
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        factory.connect();//重连
    }
}
