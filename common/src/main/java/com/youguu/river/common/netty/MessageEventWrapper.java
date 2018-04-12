package com.youguu.river.common.netty;

import com.youguu.river.common.core.HookMessageEvent;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.NameMatchMethodPointcutAdvisor;

public class MessageEventWrapper<T> extends CustomHeartbeatHandler implements MessageEventHandler, MessageEventProxy {

    final public static String proxyMappedName = "handleMessage";
    protected MessageProcessor processor;
    protected Throwable cause;
    protected HookMessageEvent<T> hook;
    protected MessageConnectFactory factory;
    private MessageEventWrapper<T> wrapper;

    public MessageEventWrapper() {

    }

    public MessageEventWrapper(MessageProcessor processor) {
        this(processor, null);
    }

    public MessageEventWrapper(MessageProcessor processor, HookMessageEvent<T> hook) {
        this.processor = processor;
        this.hook = hook;
        this.factory = processor.getMessageConnectFactory();
    }

    public void handleMessage(ChannelHandlerContext ctx, Object msg) {
        return;
    }

    public void beforeMessage(Object msg) {

    }

    public void afterMessage(Object msg) {

    }

    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        super.channelRead(ctx, msg);

        ProxyFactory weaver = new ProxyFactory(wrapper);
        NameMatchMethodPointcutAdvisor advisor = new NameMatchMethodPointcutAdvisor();
        advisor.setMappedName(MessageEventWrapper.proxyMappedName);
        advisor.setAdvice(new MessageEventAdvisor(wrapper, msg));
        weaver.addAdvisor(advisor);

        MessageEventHandler proxyObject = (MessageEventHandler) weaver.getProxy();
        proxyObject.handleMessage(ctx, msg);
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        this.cause = cause;
        cause.printStackTrace();
    }

    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
    }

    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        super.channelReadComplete(ctx);
    }

    public Throwable getCause() {
        return cause;
    }

    public void setWrapper(MessageEventWrapper<T> wrapper) {
        this.wrapper = wrapper;
    }
}
