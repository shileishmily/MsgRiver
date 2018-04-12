package com.youguu.river.producer;

import com.youguu.river.common.core.CallBackInvoker;
import com.youguu.river.common.core.HookMessageEvent;
import com.youguu.river.common.model.ResponseMessage;
import com.youguu.river.common.netty.MessageEventWrapper;
import com.youguu.river.common.netty.MessageProcessor;
import io.netty.channel.ChannelHandlerContext;

public class MessageProducerHandler extends MessageEventWrapper<String> {

    private String key;

    public MessageProducerHandler(MessageProcessor processor) {
        this(processor, null);
        super.setWrapper(this);
    }

    public MessageProducerHandler(MessageProcessor processor, HookMessageEvent hook) {
        super(processor, hook);
        super.setWrapper(this);
    }

    public void beforeMessage(Object msg) {
        key = ((ResponseMessage) msg).getMsgId();
    }

    public void handleMessage(ChannelHandlerContext ctx, Object msg) {
        if (!factory.traceInvoker(key)) {
            return;
        }

        CallBackInvoker<Object> invoker = factory.detachInvoker(key);

        if (invoker == null) {
            return;
        }

        if (this.getCause() != null) {
            invoker.setReason(getCause());
        } else {
            invoker.setMessageResult(msg);
        }
    }

    @Override
    protected void handleAllIdle(ChannelHandlerContext ctx) {
        super.handleAllIdle(ctx);
        sendPingMsg(ctx);//给Server发送心跳
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (hook != null) {
            hook.disconnect(ctx.channel().remoteAddress().toString());
        }
        super.channelInactive(ctx);
        factory.connect();//重连
    }
}
